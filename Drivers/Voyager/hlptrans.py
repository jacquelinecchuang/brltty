#!/usr/bin/python
###############################################################################
# BRLTTY - A background process providing access to the Linux console (when in
#          text mode) for a blind person using a refreshable braille display.
#
# Copyright (C) 1995-2004 by The BRLTTY Team. All rights reserved.
#
# BRLTTY comes with ABSOLUTELY NO WARRANTY.
#
# This is free software, placed under the terms of the
# GNU General Public License, as published by the Free Software
# Foundation.  Please see the file COPYING for details.
#
# Web Page: http://mielke.cc/brltty/
#
# This software is maintained by Dave Mielke <dave@mielke.cc>.
###############################################################################

import re
import sys

re_helpLine = re.compile('<HLP> (\w+):(?: "(.+?)")?? : "(.+?)" </HLP>')
# \w is any alphanumeric
# (?:...) contains stuff without forming a # match group
# +? and ?? are non-greedy forms of + and ?

# To change 'DOT1|DOT2' to 'DOT1DOT2'
re_adjacentDots = re.compile('(DOT[1-8])\\|(?=DOT)')
# (?=...) is a look-ahead assertion (does not consume the text)

# To remove key name prefixes
re_keyPrefixes = re.compile('K_|DOT')

# To change '|' to '+'
re_keyDelimiter = re.compile('\\|')

# To change '" "' to ''
re_adjacentStrings = re.compile('" *"')

# To  find common prefixes in key pairs
re_keyPair = re.compile('^((?P<prefix>.*) +)?(?P<common>.*\\+)(?P<first>.*)/(?P=common)(?P<second>.*)')
# (?P<name>...) names a gruop

def concatenateLines(first, second):
    if first and first[-1] == '\n':
        first = first[:-1]
    if first and first[-1] == '\r':
        first = first[:-1]
    return first + second

inputLines = sys.stdin.readlines()
helpText = reduce(concatenateLines, inputLines, '')
helpLines = re_helpLine.findall(helpText)
for where,keys,description in helpLines:
    if keys:
        keys = re_adjacentDots.sub('\\1', keys)
        keys = re_keyPrefixes.sub('', keys)
        keys = re_keyDelimiter.sub('+', keys)
        keys = re_adjacentStrings.sub('', keys)
        pair = re_keyPair.search(keys)
        if pair:
            keys = pair.group('common') + ' ' + pair.group('first') + '/' + pair.group('second')
            prefix = pair.group('prefix')
            if prefix:
                keys = prefix + keys
    description = re_adjacentStrings.sub('', description)
    if keys:
        sys.stdout.write(where + ':' + keys + ': ' + description + '\n')
    else:
        sys.stdout.write(where + ':' + description + '\n')
