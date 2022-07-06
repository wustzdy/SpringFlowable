package com.wustzdy.springboot.flowable.demo.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BaseUtils {

    /**
     * 普通工单流程的截止时间
     */
    public static Date getDueDateForGeneralProcedure(int minutes, Date sendOrderDate) {
        int days = 0;
        Calendar calendar = Calendar.getInstance();
        Calendar calDueDate = Calendar.getInstance();
        Calendar calSendDate = Calendar.getInstance();
        //初始化日期
        calDueDate.setTime(sendOrderDate);//截止日期
        calSendDate.setTime(sendOrderDate);//派单时间
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calDueDate.add(Calendar.MINUTE, minutes);
        calDueDate.set(Calendar.HOUR_OF_DAY, 0);
        calDueDate.set(Calendar.MINUTE, 0);
        calDueDate.set(Calendar.SECOND, 0);
        //System.out.println(df.format(calEnd.getTime()));
        while (calSendDate.before(calDueDate)) {
            days++;
            calSendDate.add(Calendar.DAY_OF_YEAR, 1);
        }
        //System.out.println("day=="+days);
        Date finalDate = calSendDate.getTime();
        if (days > 0) {
            //计算工作日，不包括 周六周日，返回一个日期
            //这个方法返回 截止日期的年月日部分
            finalDate = addWorkDay(sendOrderDate, days);
        }
        //重新设置截止日期，只是为了取最后截止日期的时分秒
        calDueDate.setTime(sendOrderDate);
        calDueDate.add(Calendar.MINUTE, minutes);
        //设置最终的返回日期
        calendar.setTime(finalDate);
        calendar.set(Calendar.HOUR_OF_DAY, calDueDate.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calDueDate.get(Calendar.MINUTE));
        return calendar.getTime();
    }

    /**
     * 计算工作日，不包括 周六周日
     * <p>
     * Author YangHg
     *
     * @param date start time
     * @param days add days
     * @return
     */
    private static Date addWorkDay(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int mod = days % 5;
        int other = days / 5 * 7;
        for (int i = 0; i < mod; ) {
            calendar.add(Calendar.DATE, 1);
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY:
                case Calendar.SATURDAY:
                    break;
                default:
                    i++;
                    break;
            }
        }
        if (other > 0) {
            calendar.add(Calendar.DATE, other);
        }
        return calendar.getTime();
    }

    /**
     * 判断变量是否为空
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        if (null == s || "".equals(s) || "".equals(s.trim()) || "null".equalsIgnoreCase(s)) {
            return true;
        } else {
            return false;
        }
    }

    public static Date getReqCompTime(int reqDealLimit) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        // 测试完请屏蔽， 测试用为方便测试计算将分设为0
//		c.set(Calendar.MINUTE, 0);
//		c.set(Calendar.HOUR, 3);
//		System.out.println(new Timestamp(c.getTimeInMillis()));
        //获取当前小时
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        /**
         * 派单时间介于0点到6点
         */
        if (currentHour < 6) {
            //设置成时间为6点，再计算完成时限
            c.set(Calendar.HOUR, 6);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            //如果时限长，还得考虑是否到第二天的0到6点，24-6=18小时
            if (reqDealLimit > 18 * 60) {
                caculateReqCompTime(c, reqDealLimit * 60);
            } else {
                c.add(Calendar.MINUTE, reqDealLimit);
            }
        } else {
            //获取当前分钟
            int currentMin = c.get(Calendar.MINUTE);
            //获取当前秒
            int currentScd = c.get(Calendar.SECOND);
            //与24点相差的小时数
            int hourLeft = 23 - currentHour;
            //当前时间与24:00:00间隔的秒数
            int secondLeft = hourLeft * 3600 + (59 - currentMin) * 60 + 60 - currentScd;
            //时限秒数
            int dealLimitScd = reqDealLimit * 60;
            System.out.println("距离24：00：00 还有" + secondLeft / 60 + "秒");
            System.out.println("距离24：00：00 还有" + secondLeft + "秒");
            System.out.println("要求完成时限为：" + dealLimitScd + "秒");
            //条件成立则直接加上时限不计算0点到6点的时间
            if (dealLimitScd > secondLeft) {
                int restScd = dealLimitScd - secondLeft;
                c.add(Calendar.SECOND, secondLeft);//24:00:00
                c.set(Calendar.SECOND, 0);
                c.add(Calendar.HOUR, 6);//6:00:00
                //如果时限剩余分钟数仍大于18个小时，则还需跨过0点到6点，重新计算一次
                if (restScd > 18 * 3600) {
                    caculateReqCompTime(c, restScd);
                } else {
                    c.add(Calendar.SECOND, restScd);
                }
            } else {
                c.add(Calendar.MINUTE, reqDealLimit);
            }
        }
        return new Timestamp(c.getTimeInMillis());
    }

    private static void caculateReqCompTime(Calendar c, int restScd) {
        //将剩余时限减去18小时
        restScd = restScd - 18 * 3600;
        System.out.println(restScd);
        c.add(Calendar.HOUR, 24);
        //如果时限剩余分钟数仍大于18个小时，则还需跨过0点到6点，重新计算一次，直到不跨0点到6点
        if (restScd > 18 * 3600) {
            caculateReqCompTime(c, restScd);
        }
        c.add(Calendar.SECOND, restScd);
    }

}
