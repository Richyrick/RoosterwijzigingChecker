/*
 *    This file is part of RSG-Wijzigingen.
 *
 *     RSG-Wijzigingen is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RSG-Wijzigingen is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with RSG-Wijzigingen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rickendirk.rsgwijzigingen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isAlarmsEnabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("pref_auto_zoek", false);
        if (isAlarmsEnabled) {
            setupCalendarTimes(context);
            OwnAlarmsManager.setupAlarms(context);
        }
        check1ekeerSindsVersie140(context, isAlarmsEnabled);
    }

    private void check1ekeerSindsVersie140(Context context, boolean isAlarmsEnabled) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wel1ekeer = sp.getBoolean("1eKeerSinds140", true);
        if (wel1ekeer){
            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putBoolean("1eKeerSinds140", false);
            spEditor.commit();
            if (isAlarmsEnabled) {
                OwnAlarmsManager.cancelAlarms(context);
                OwnAlarmsManager.setupAlarms(context);
            }
        }
    }

    private void setupCalendarTimes(Context context) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        long timeInMsCal1 = sp.getLong("timeMs1", 0);
        long timeInMsCal2 = sp.getLong("timeMs2", 0);

        calendar1.setTimeInMillis(timeInMsCal1);
        calendar2.setTimeInMillis(timeInMsCal2);

        //Datum mag niet voor huidige datumn zijn, daat gaat hij direct
        boolean isVeranderd = false;
        if (calendar1.before(Calendar.getInstance())) {
            calendar1.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
            isVeranderd = true;
        }

        if (calendar2.before(Calendar.getInstance())) {
            calendar2.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
            isVeranderd = true;
        }

        if (isVeranderd){
            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putLong("timeMs1", calendar1.getTimeInMillis());
            spEditor.putLong("timeMs2", calendar2.getTimeInMillis());
            spEditor.commit();
        }

    }
}
