package com.example.demo;

import com.example.demo.bean.Department;
import com.example.demo.bean.Employee;
import com.example.demo.mapper.DepartmentMapper;
import com.example.demo.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest
class DemoApplicationTests {

    /********************Log*******************/
    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void contextLoads_log() {
        //日志的级别；
        //由低到高   trace<debug<info<warn<error
        //可以调整输出的日志级别；日志就只会在这个级别以以后的高级别生效
        logger.trace("这是trace日志...");
        logger.debug("这是debug日志...");
        //SpringBoot默认给我们使用的是info级别的，没有指定级别的就用SpringBoot默认规定的级别；root级别
        logger.info("这是info日志...");
        logger.warn("这是warn日志...");
        logger.error("这是error日志...");
    }

    /********************Datasource*******************/
    @Autowired
    DataSource dataSource;

    @Test
    public void contextLoads_datasource() throws SQLException {
        System.out.println(dataSource.getClass());
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    /********************Mybatis*******************/
    @Autowired(required = false)
    DepartmentMapper departmentMapper;

    @Test
    public void contextLoads_mybatisInter() {
        Department department = new Department("testDepartment");
        departmentMapper.insertDept(department);
    }

    @Autowired(required = false)
    EmployeeMapper employeeMapper;

    @Test
    public void contextLoads_mybatisXml() {
        Department department = new Department("testDepartment");
        departmentMapper.insertDept(department);

        Employee employee = new Employee(1, "lastName", "email", 1, 1);
        employeeMapper.insertEmp(employee);

        Employee employeeResult = employeeMapper.getEmpById(1);
        System.out.println(employeeResult.getLastName());
    }

    /********************Redis*******************/
    @Autowired
    StringRedisTemplate stringRedisTemplate;  //操作k-v都是字符串的

    @Autowired
    RedisTemplate redisTemplate;  //k-v都是对象的

    @Autowired(required = false)
    RedisTemplate<Object, Employee> empRedisTemplate;

    /**
     * Redis常见的五大数据类型
     * String（字符串）、List（列表）、Set（集合）、Hash（散列）、ZSet（有序集合）
     * stringRedisTemplate.opsForValue()[String（字符串）]
     * stringRedisTemplate.opsForList()[List（列表）]
     * stringRedisTemplate.opsForSet()[Set（集合）]
     * stringRedisTemplate.opsForHash()[Hash（散列）]
     * stringRedisTemplate.opsForZSet()[ZSet（有序集合）]
     */
    @Test
    public void contextLoads_redisString() {
        //给redis中保存数据
        stringRedisTemplate.opsForValue().append("msg", "hello");
        String msg = stringRedisTemplate.opsForValue().get("msg");
        System.out.println(msg);

        stringRedisTemplate.opsForList().leftPush("mylist", "1");
        stringRedisTemplate.opsForList().leftPush("mylist", "2");
    }

    //测试保存对象
    @Test
    public void contextLoads_redisObject() {
        Employee employee = new Employee(1, "lastName", "email", 1, 1);
        //默认如果保存对象，使用jdk序列化机制，序列化后的数据保存到redis中
        //redisTemplate.opsForValue().set("emp-01",employee);

        //1、将数据以json的方式保存
        //(1)自己将对象转为json
        //(2)redisTemplate默认的序列化规则；改变默认的序列化规则；
        empRedisTemplate.opsForValue().set("emp-01", employee);
    }

    /********************RabbitMQ*******************/
    /**
     * 自动配置
     * 1、RabbitAutoConfiguration
     * 2、有自动配置了连接工厂ConnectionFactory；
     * 3、RabbitProperties 封装了 RabbitMQ的配置
     * 4、 RabbitTemplate ：给RabbitMQ发送和接受消息；
     * 5、 AmqpAdmin ： RabbitMQ系统管理功能组件;
     * AmqpAdmin：创建和删除 Queue，Exchange，Binding
     * 6、@EnableRabbit +  @RabbitListener 监听消息队列的内容
     */

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    AmqpAdmin amqpAdmin;


    //todo 创建一个directExchange， fanoutExchange
    @Test
    public void contextLoads_createExchange() {
        amqpAdmin.declareExchange(new DirectExchange("amqpadmin.directExchange"));
        amqpAdmin.declareExchange(new FanoutExchange("amqpadmin.fanoutExchange"));

        amqpAdmin.declareQueue(new Queue("amqpadmin.queue1", true));
        amqpAdmin.declareQueue(new Queue("amqpadmin.queue2", true));

        //创建绑定规则
        amqpAdmin.declareBinding(new Binding("amqpadmin.queue1",
                Binding.DestinationType.QUEUE,
                "amqpadmin.directExchange",
                "direct",
                null));

        amqpAdmin.declareBinding(new Binding("amqpadmin.queue1",
                Binding.DestinationType.QUEUE,
                "amqpadmin.fanoutExchange",
                "fanout",
                null));

        amqpAdmin.declareBinding(new Binding("amqpadmin.queue2",
                Binding.DestinationType.QUEUE,
                "amqpadmin.fanoutExchange",
                "fanout",
                null));

    }

    @Test
    public void contextLoads_Send() {
        //Message需要自己构造一个;定义消息体内容和消息头
        //rabbitTemplate.send(exchage,routeKey,message);

        //object默认当成消息体，只需要传入要发送的对象，自动序列化发送给rabbitmq；
        //rabbitTemplate.convertAndSend(exchage,routeKey,object);

        Employee employee = new Employee(1, "lastName", "email", 1, 1);
        //对象被默认序列化以后发送出去
        rabbitTemplate.convertAndSend("amqpadmin.fanoutExchange","direct",employee);
    }

    @Test
    public void contextLoads_receive(){
        Object o = rabbitTemplate.receiveAndConvert("amqpadmin.queue1");
        System.out.println(o.getClass());
        System.out.println(o);
    }


}
