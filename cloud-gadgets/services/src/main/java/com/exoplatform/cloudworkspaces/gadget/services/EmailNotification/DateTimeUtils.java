/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.Calendar;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * Aug 16, 2012  
 */
public class DateTimeUtils {

  public static long nextMinuteOf(long date) {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(date);
    now.add(Calendar.MINUTE, 1);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return now.getTimeInMillis();
  }

  public static long nextDayOf(long date) {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(date);
    now.add(Calendar.DAY_OF_YEAR, 1);

    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return now.getTimeInMillis();
  }

  public static long nextMondayOf(long date) {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(date);
    int weekday = now.get(Calendar.DAY_OF_WEEK);
    int days = weekday == Calendar.SUNDAY ? 1 : Calendar.SATURDAY - weekday + 2;
    now.add(Calendar.DAY_OF_YEAR, days);

    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return now.getTimeInMillis();
  }

  public static long nextMonthOf(long date) {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(date);
    now.add(Calendar.MONTH, 1);
    now.set(Calendar.DAY_OF_MONTH, 1);

    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return now.getTimeInMillis();
  }
  
  public static long subtract7days(long date) {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(date);
    now.add(Calendar.DAY_OF_YEAR, -7);

    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return now.getTimeInMillis();
  }

  public static long subtract38days(long date) {
    Calendar now = Calendar.getInstance();
    now.setTimeInMillis(date);
    //
    now.add(Calendar.DAY_OF_YEAR, -38);

    now.set(Calendar.HOUR_OF_DAY, 0);
    now.set(Calendar.MINUTE, 0);
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    return now.getTimeInMillis();
  }

}
