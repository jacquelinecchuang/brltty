/*
 * BRLTTY - A background process providing access to the console screen (when in
 *          text mode) for a blind person using a refreshable braille display.
 *
 * Copyright (C) 1995-2018 by The BRLTTY Developers.
 *
 * BRLTTY comes with ABSOLUTELY NO WARRANTY.
 *
 * This is free software, placed under the terms of the
 * GNU Lesser General Public License, as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any
 * later version. Please see the file LICENSE-LGPL for details.
 *
 * Web Page: http://brltty.com/
 *
 * This software is maintained by Dave Mielke <dave@mielke.cc>.
 */

package org.a11y.brltty.android;

import java.util.Arrays;
import java.util.Collection;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.LinkedHashMap;

import java.util.Set;

import java.text.Collator;
import java.text.CollationKey;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;

public abstract class SettingsFragment extends PreferenceFragment {
  private static final String LOG_TAG = SettingsFragment.class.getName();

  public static Map<String, String> getProperties (String owner, SharedPreferences prefs, String... keys) {
    Map<String, String> properties = new LinkedHashMap();

    for (String key : keys) {
      properties.put(key, prefs.getString(String.format(key, owner), ""));
    }

    return properties;
  }

  public static void putProperties (String owner, SharedPreferences.Editor editor, Map<String, String> properties) {
    for (Map.Entry<String, String> property : properties.entrySet()) {
      editor.putString(String.format(property.getKey(), owner), property.getValue());
    }
  }

  public static void removeProperties (String owner, SharedPreferences.Editor editor, String... keys) {
    for (String key : keys) {
      editor.remove(String.format(key, owner));
    }
  }

  private static Collator localeCollator = null;

  @Override
  public void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  protected final Preference getPreference (int key) {
    return findPreference(getResources().getString(key));
  }

  protected final PreferenceScreen getPreferenceScreen (int key) {
    return (PreferenceScreen)getPreference(key);
  }

  protected final CheckBoxPreference getCheckBoxPreference (int key) {
    return (CheckBoxPreference)getPreference(key);
  }

  protected final EditTextPreference getEditTextPreference (int key) {
    return (EditTextPreference)getPreference(key);
  }

  protected final ListPreference getListPreference (int key) {
    return (ListPreference)getPreference(key);
  }

  protected final MultiSelectListPreference getMultiSelectListPreference (int key) {
    return (MultiSelectListPreference)getPreference(key);
  }

  protected final void showListSelection (ListPreference list) {
    CharSequence label = list.getEntry();

    if (label == null) {
      label = "";
    }

    list.setSummary(label);
  }

  protected final void showListSelection (ListPreference list, int index) {
    list.setSummary(list.getEntries()[index]);
  }

  protected final int getListIndex (ListPreference list, String value) {
    return Arrays.asList(list.getEntryValues()).indexOf(value);
  }

  protected final void showListSelection (ListPreference list, String value) {
    showListSelection(list, getListIndex(list, value));
  }

  protected final void showSetSelections (MultiSelectListPreference set, Set<String> values) {
    StringBuilder label = new StringBuilder();

    if (values.size() > 0) {
      CharSequence[] labels = set.getEntries();

      for (String value : values) {
        if (value.length() > 0) {
          if (label.length() > 0) {
            label.append('\n');
          }

          label.append(labels[set.findIndexOfValue(value)]);
        }
      }
    } else {
      label.append(getString(R.string.SET_SELECTION_NONE));
    }

    set.setSummary(label.toString());
  }

  protected final void showSetSelections (MultiSelectListPreference set) {
    showSetSelections(set, set.getValues());
  }

  protected final void resetList (ListPreference list) {
    list.setValueIndex(0);
    showListSelection(list);
  }

  protected final void setListElements (ListPreference list, String[] values, String[] labels) {
    list.setEntryValues(values);
    list.setEntries(labels);
  }

  protected final void setListElements (ListPreference list, String[] values) {
    setListElements(list, values, values);
  }

  protected final void sortList (ListPreference list, int fromIndex) {
    if (localeCollator == null) {
      localeCollator = Collator.getInstance();
      localeCollator.setStrength(Collator.PRIMARY);
      localeCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
    }

    String[] values = LanguageUtilities.newStringArray(list.getEntryValues());
    String[] labels = LanguageUtilities.newStringArray(list.getEntries());

    int size = values.length;
    Map<String, String> map = new LinkedHashMap<String, String>();
    CollationKey keys[] = new CollationKey[size];

    for (int i=0; i<size; i+=1) {
      String label = labels[i];
      map.put(label, values[i]);
      keys[i] = localeCollator.getCollationKey(label);
    }

    Arrays.sort(keys, fromIndex, keys.length);

    for (int i=0; i<size; i+=1) {
      String label = keys[i].getSourceString();
      labels[i] = label;
      values[i] = map.get(label);
    }

    setListElements(list, values, labels);
  }

  protected final void sortList (ListPreference list) {
    sortList(list, 0);
  }

  protected final SharedPreferences getSharedPreferences () {
    return getPreferenceManager().getDefaultSharedPreferences(getActivity());
  }
}