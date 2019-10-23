package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
@Slf4j
public class ChooseCourseTask {

    @Autowired
    private TaskService taskService;

    @Scheduled(cron = "0/10 * * * * *")
    //定时发送加选课任务
    public void sendChooseCourseTask() {
        //得到一分钟之前的消息列表
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE, -1);
        // calendar.set(GregorianCalendar.MINUTE,-1);
        Date updateTime = calendar.getTime();
        List<XcTask> taskList = taskService.findXcTaskList(updateTime, 100);
        //调用service发布消息，将添加选课的任务发送给mq
        // System.out.println(taskList);
        for (XcTask xcTask : taskList) {
            if (taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0) {
                //要发送的交换机
                String mqExchange = xcTask.getMqExchange();
                //发送消息要携带的routingKey
                String mqRoutingkey = xcTask.getMqRoutingkey();
                //发送消息
                taskService.publish(xcTask, mqExchange, mqRoutingkey);
                log.info("send choose course task id:{}",xcTask.getId());
            }
        }
    }

    // @Scheduled(cron = "0/3 * * * * *")
    // @Scheduled(fixedRate = 3000)//在任务开始后3秒执行下一次调度
    // @Scheduled(fixedDelay = 3000)//在任务结束后3秒执行下一次调度
    public void task1() {
        log.info("======================测试定时任务1开始======================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("======================测试定时任务1结束======================");
    }

    // @Scheduled(cron = "0/3 * * * * *")
    public void task2() {
        log.info("======================测试定时任务2开始======================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("======================测试定时任务2结束======================");
    }

    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveFinishChooseCourseTask(XcTask xcTask) {
        if (xcTask != null && StringUtils.isNotEmpty(xcTask.getId())) {
            taskService.finishTask(xcTask.getId());
        }
    }


}
