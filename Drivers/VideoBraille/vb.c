/*
 * BRLTTY - A background process providing access to the Linux console (when in
 *          text mode) for a blind person using a refreshable braille display.
 *
 * Copyright (C) 1995-2003 by The BRLTTY Team. All rights reserved.
 *
 * BRLTTY comes with ABSOLUTELY NO WARRANTY.
 *
 * This is free software, placed under the terms of the
 * GNU General Public License, as published by the Free Software
 * Foundation.  Please see the file COPYING for details.
 *
 * Web Page: http://mielke.cc/brltty/
 *
 * This software is maintained by Dave Mielke <dave@mielke.cc>.
 */

/* Thanks to the authors of the Vario-HT driver: the implementation of this
 * driver is similar to the Vario-HT one.
 */

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif /* HAVE_CONFIG_H */

#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>

#include "Programs/brl.h"
#include "Programs/misc.h"

#include "Programs/brl_driver.h"
#include "braille.h"
#include "vblow.h"

static unsigned char lastbuff[40];

static void brl_identify(void) {
  LogPrint(LOG_NOTICE, "VideoBraille Driver");
}

static int brl_open(BrailleDisplay *brl, char **parameters, const char *dev) {
  /*	Seems to signal en error */ 
  if (!vbinit()) {
    /* Theese are pretty static */ 
    brl->x=40;
    brl->y=1;
    return 1;
  }
  return 0;
}

static void brl_close(BrailleDisplay *brl) {
}

static void brl_writeWindow(BrailleDisplay *brl) {
  char outbuff[40];
  int i;

  if (!brl) {
    return;
  }
  /* Only display something if the data actually differs, this 
  *  could most likely cause some problems in redraw situations etc
  *  but since the darn thing wants to redraw quite frequently otherwise 
  *  this still makes a better lookin result */ 
  for (i = 0; i<40; i++) {
    if (lastbuff[i]!=brl->buffer[i]) {
      memcpy(lastbuff,brl->buffer,40*sizeof(char));
      /*  Redefine the given dot-pattern to match ours */
      vbtranslate(brl->buffer, outbuff, 40);
      vbdisplay(outbuff);
      vbdisplay(outbuff);
      shortdelay(VBREFRESHDELAY);
      break;
    }
  }
}

static void brl_writeStatus (BrailleDisplay *brl, const unsigned char *st) {
/* The VideoBraille display has no status cells */
}

static int brl_readCommand(BrailleDisplay *brl, DriverCommandContext cmds) {
  vbButtons buttons;
  BrButtons(&buttons);
  if (!buttons.keypressed) {
    return EOF;
  } else {
    vbButtons b;
    do {
      BrButtons(&b);
      buttons.bigbuttons |= b.bigbuttons;
      usleep(1);
    } while (b.keypressed);
    /* Test which buttons has been pressed */
    if (buttons.bigbuttons==KEY_UP) return CMD_LNUP;
    else if (buttons.bigbuttons==KEY_LEFT) return CMD_FWINLT;
    else if (buttons.bigbuttons==KEY_RIGHT) return CMD_FWINRT;
    else if (buttons.bigbuttons==KEY_DOWN) return CMD_LNDN;
    else if (buttons.bigbuttons==KEY_ATTRIBUTES) return CMD_ATTRVIS;
    else if (buttons.bigbuttons==KEY_CURSOR) return CMD_CSRVIS;
    else if (buttons.bigbuttons==KEY_HOME) {
      /* If a routing key has been pressed, then mark the beginning of a block;
         go to cursor position otherwise */
      return (buttons.routingkey>0) ? CR_CUTBEGIN+buttons.routingkey-1 : CMD_HOME;
    }
    else if (buttons.bigbuttons==KEY_MENU) {
      /* If a routing key has been pressed, then mark the end of a block;
         go to preferences menu otherwise */
      return (buttons.routingkey>0) ? CR_CUTRECT+buttons.routingkey-1 : CMD_PREFMENU;
    }
    else if (buttons.bigbuttons==(KEY_ATTRIBUTES | KEY_MENU)) return CMD_PASTE;
    else if (buttons.bigbuttons==(KEY_CURSOR | KEY_LEFT)) return CMD_CHRLT;
    else if (buttons.bigbuttons==(KEY_HOME | KEY_RIGHT)) return CMD_CHRRT;
    else if (buttons.bigbuttons==(KEY_UP | KEY_LEFT)) return CMD_TOP_LEFT;
    else if (buttons.bigbuttons==(KEY_RIGHT | KEY_DOWN)) return CMD_BOT_LEFT;
    else if (buttons.bigbuttons==(KEY_ATTRIBUTES | KEY_DOWN)) return CMD_HELP;
    else if (buttons.bigbuttons==(KEY_MENU | KEY_CURSOR)) return CMD_INFO;
    else if (buttons.bigbuttons==0) {
      /* A cursor routing key has been pressed */
      if (buttons.routingkey>0) {
        usleep(5);
        return CR_ROUTE+buttons.routingkey-1;
      }
      else return EOF;
    } else
      return EOF;
  }
}
