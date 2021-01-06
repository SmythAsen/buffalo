import com.asen.buffalo.date.DateUtils;
import org.junit.Test;

import java.time.Duration;
import java.time.Period;
import java.util.Date;


public class DateUtilsTest {

    @Test

    public void betweenDays() {
        Date startDate = DateUtils.parse("2020-05-14 15:00:00");
        Date endDate = new Date();
        Period period = DateUtils.betweenDays(startDate, endDate);
        System.out.println(period.getDays());
        Duration duration = DateUtils.betweenTimes(startDate, endDate);
        System.out.println(duration.getSeconds());
    }

    @Test
    public void startWeekDate() {
        Date date = DateUtils.startWeekDate(new Date());
        System.out.println(DateUtils.format(date));
    }
}
