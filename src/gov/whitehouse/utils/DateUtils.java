/*
 * This project constitutes a work of the United States Government and is
 * not subject to domestic copyright protection under 17 USC § 105.
 * 
 * However, because the project utilizes code licensed from contributors
 * and other third parties, it therefore is licensed under the MIT
 * License.  http://opensource.org/licenses/mit-license.php.  Under that
 * license, permission is granted free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the conditions that any appropriate copyright notices and this
 * permission notice are included in all copies or substantial portions
 * of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package gov.whitehouse.utils;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.ERA;
import static java.util.Calendar.YEAR;

public class DateUtils {

    public static Calendar getDayBefore(final Calendar target) {
        target.add(Calendar.DAY_OF_YEAR, -1);
        return target;
    }

    public static boolean isSameDay(final Calendar firstCal, final Calendar secondCal) {
        return (firstCal.get(ERA) == secondCal.get(ERA) &&
                firstCal.get(YEAR) == secondCal.get(YEAR) &&
                firstCal.get(DAY_OF_YEAR) == secondCal.get(DAY_OF_YEAR));
    }

    public static boolean isSameDay(final Date firstDate, final Date secondDate) {
        final Calendar firstCal = Calendar.getInstance();
        final Calendar secondCal = Calendar.getInstance();
        firstCal.setTime(firstDate);
        secondCal.setTime(secondDate);
        return isSameDay(firstCal, secondCal);
    }

    public static boolean isToday(final Calendar queryCal) {
        return isSameDay(Calendar.getInstance(), queryCal);
    }

    public static boolean isToday(final Date queryDate) {
        final Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(queryDate);
        return isToday(queryCal);
    }

    public static boolean isYesterday(final Calendar queryCal) {
        return isSameDay(getDayBefore(Calendar.getInstance()), queryCal);
    }

    public static boolean isYesterday(final Date queryDate) {
        final Calendar queryCal = Calendar.getInstance();
        queryCal.setTime(queryDate);
        return isYesterday(queryCal);
    }
}
