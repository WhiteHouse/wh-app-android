package gov.whitehouse.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.ERA;
import static java.util.Calendar.YEAR;

public
class DateUtils
{

    public static final long SECOND_IN_MILLIS = 1000;

    public static final SimpleDateFormat WH_DATE_FORMAT
            = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    public static
    Calendar getDayBefore(final Calendar target)
    {
        target.add(Calendar.DAY_OF_YEAR, -1);
        return target;
    }

    public static
    boolean isSameDay(final Calendar firstCal, final Calendar secondCal)
    {
        return (firstCal.get(ERA) == secondCal.get(ERA) &&
                firstCal.get(YEAR) == secondCal.get(YEAR) &&
                firstCal.get(DAY_OF_YEAR) == secondCal.get(DAY_OF_YEAR));
    }

    public static
    boolean isSameDay(final Date firstDate, final Date secondDate)
    {
        final Calendar firstCal = Calendar.getInstance();
        final Calendar secondCal = Calendar.getInstance();
        firstCal.setTime(firstDate);
        secondCal.setTime(secondDate);
        return isSameDay(firstCal, secondCal);
    }

    public static
    boolean isToday(final Calendar queryCal)
    {
        return isSameDay(Calendar.getInstance(), queryCal);
    }

    public static
    boolean isToday(final Date queryDate)
    {
        final Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(queryDate);
        return isToday(queryCal);
    }

    public static
    boolean isYesterday(final Calendar queryCal)
    {
        return isSameDay(getDayBefore(Calendar.getInstance()), queryCal);
    }

    public static
    boolean isYesterday(final Date queryDate)
    {
        final Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(queryDate);
        return isYesterday(queryCal);
    }

    public static
    boolean isSomeDayBefore(final Date queryDate)
    {
        final Calendar todayCal = Calendar.getInstance();
        final Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(queryDate);
        return (todayCal.get(Calendar.YEAR) > queryCal.get(Calendar.YEAR)) ||
                (todayCal.get(Calendar.DAY_OF_YEAR) > queryCal.get(Calendar.DAY_OF_YEAR));
    }

    public static
    boolean isFutureDay(final Date queryDate)
    {
        final Calendar todayCal = Calendar.getInstance();
        final Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(queryDate);
        return (todayCal.get(Calendar.YEAR) < queryCal.get(Calendar.YEAR)) ||
                (!(todayCal.get(Calendar.YEAR) > queryCal.get(Calendar.YEAR)) &&
                (todayCal.get(Calendar.DAY_OF_YEAR) < queryCal.get(Calendar.DAY_OF_YEAR)));
    }

    public static
    Date parseDate(String date)
    {
        try {
            return WH_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            Timber.w(e, "Couldn't parse date '%s'", date);
            return null;
        }
    }

    public static
    String formatDate(Date date)
    {
        return WH_DATE_FORMAT.format(date);
    }

    public static
    boolean within30MinutesBeforeNow(Date date)
    {
        final Date now = new Date();
        final long diff = now.getTime() - date.getTime();
        return (diff > 0) && (diff < (SECOND_IN_MILLIS * 60 * 30));
    }
}
