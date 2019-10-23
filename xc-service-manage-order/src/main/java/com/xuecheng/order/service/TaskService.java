package com.xuecheng.order.service;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private XcTaskRepository xcTaskRepository;
    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    //查询前n条任务
    public List<XcTask> findXcTaskList(Date updateTime,int size) {
        //设置分页参数
        Pageable pageable = PageRequest.of(0, size);
        //查询前n条任务
        Page<XcTask> all = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return all.getContent();
    }

    //发布消息
    @Transactional
    public void publish(XcTask xcTask,String ex,String routingKey) {
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if (optional.isPresent()) {
            //发送消息
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            XcTask one = optional.get();
            //更新任务时间
            one.setUpdateTime(new Date());
            xcTaskRepository.save(one);
        }
    }

    @Transactional
    public int getTask(String id,int version) {
        //通过乐观锁的方式来更新数据表，如果结果大于0说明取到任务
        return xcTaskRepository.updateTaskVersion(id, version);
    }

    @Transactional
    public void finishTask(String taskId) {
        Optional<XcTask> optional = xcTaskRepository.findById(taskId);
        if (optional.isPresent()) {
            //当前任务
            XcTask xcTask = optional.get();
            //历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }
}
