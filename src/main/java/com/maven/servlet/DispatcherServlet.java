package com.maven.servlet;

import com.maven.annotation.Autowired;
import com.maven.annotation.Controller;
import com.maven.annotation.RequestMapping;
import com.maven.annotation.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    //保存Controller对象和RequestMapping之间的映射关系
    private Map<String, Object> beans = new HashMap<>();

    //保存路径对象和函数方法之间的映射关系
    private Map<String, Object> mapping = new HashMap<>();

    //classNames保存类名
    private List<String> classNames = new ArrayList<>();


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //获取URL: %spring-mvc%/test/index
        String url = request.getRequestURI();

        // springmvc ,对以上的%spring-mvc%进行替换
        String context = request.getContextPath();

        //path = /test/index  --映射为-> com.main.controller.TestController.index()
        String path = url.replaceAll(context,"");

        Method method = (Method) mapping.get(path);

        //取出对象
        Object instance = beans.get("/"+path.split("/")[1]);

        try {
            method.invoke(instance,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1、扫描包
     * 2、类初始化
     * 3、依赖注入
     * 4、映射关系实现
     */
    @Override
    public void init() throws ServletException {

        //1、扫描包
        scanPackage("com.maven");

        for (String name : classNames) {
            System.out.println(name);
        }
        //2、类初始化
        createInstance();

        //3、依赖注入
        injection();

        //4、映射关系实现
        handlerMapping();
    }

    private void handlerMapping() {
        //bean容器为空
        if (beans.isEmpty()) {
            System.out.println("Nothing bean,go to bed please!");
            return;
        }

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            //取得对象
            Object instance = entry.getValue();

            if (instance.getClass().isAnnotationPresent(RequestMapping.class)) {
                //获取类上的RequestMapping
                RequestMapping rm = instance.getClass().getAnnotation(RequestMapping.class);

                //获取类上RequestMapping的value值
                String m1 = rm.value();

                //取出所有的Method
                Method[] methods = instance.getClass().getDeclaredMethods();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        //获取方法上的RequestMapping
                        RequestMapping rm2 = method.getAnnotation(RequestMapping.class);

                        //获取方法上RequestMapping的value值
                        String m2 = rm2.value();

                        //保存路径（m1+m2）和URL的映射
                        mapping.put(m1 + m2, method);

                    }
                }
            }
        }
    }

    /**
     * 依赖注入
     */
    private void injection() {
        //bean容器为空
        if (beans.isEmpty()) {
            System.out.println("Nothing bean,go to bed please!");
            return;
        }

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            //取出对象
            Object instance = entry.getValue();
            //使用反射获取类的上字段信息
            Field[] fields = instance.getClass().getDeclaredFields();
            //遍历所有字段，找出包含有Autowire注解的字段
            for (Field field : fields) {
                //判断字段上是否存在Autowired注解
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);

                    //获取Autowired的value值
                    String value = autowired.value();

                    //是否启用权限检查
                    field.setAccessible(true);
                    try {
                        field.set(instance, beans.get(value));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createInstance() {
        //判断 classNames容器为空 没有 class文件
        if (classNames.isEmpty()) {
            System.out.println("Nothing,go to bed please!");
            return;
        }

        //将 String ---》 ClassName ---》 Object初始化（new/newInstance）
        for (String name : classNames) {
            String className = name.replace(".class", "");


            try {
                //类加载
                Class<?> clazz = Class.forName(className);

                //判断该类上是否存在注解Controller
                if (clazz.isAnnotationPresent(Controller.class)) {
                    //实例化该Class
                    Object instance = clazz.newInstance();

                    //扫描Controller上的RequestMapping，并获取RequestMapping上的value值
                    RequestMapping rm = clazz.getAnnotation(RequestMapping.class);
                    String value = rm.value();

                    //保存映射关系
                    beans.put(value, instance);
                }

                //判断该类上是否存在注解Service
                if (clazz.isAnnotationPresent(Service.class)) {
                    //实例化
                    Object instance = clazz.newInstance();

                    //获取Service注解类
                    Service service = clazz.getAnnotation(Service.class);
                    //获取该Service注解类上的value值
                    String value = service.value();
                    //保存映射关系
                    beans.put(value, instance);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 扫描包，将packageName路径下的所有目录的
     *
     * @param packageName
     */
    private void scanPackage(String packageName) {
//        System.out.println(packageName);

//        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(packageName);

        String path = packageName.replaceAll("\\.", "/");

        URL url = getClass().getClassLoader().getResource("/" + path);

        File[] files = new File(url.getFile()).listFiles();

        for (File file : files) {

            //对下级目录进行递归
            if (file.isDirectory()) {
                //进入目录找文件
                scanPackage(packageName + "." + file.getName());
            } else {
                //将扫描到的文件加入classNames
                classNames.add(packageName + "." + file.getName());
            }
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doPost(request, response);
    }
}
