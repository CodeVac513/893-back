//package com.samyookgoo.palgoosam.common.lock;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class LockRetryHandler {
//    private final ConcurrentHashMap<String, Queue<TaskWrapper>> waiters = new ConcurrentHashMap<>();
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();
//
//    public void register(String lockKey, TaskWrapper wrapper) {
//        log.info("LockRetryHandler: register 호출 - lockKey={}", lockKey);
//        waiters.computeIfAbsent(lockKey, k -> new ConcurrentLinkedQueue<>()).add(wrapper);
//    }
//
//    public void retry(String lockKey) {
//        log.info("LockRetryHandler: retry 호출");
//        Queue<TaskWrapper> queue = waiters.get(lockKey);
//        if (queue != null) {
//            TaskWrapper next = queue.poll();
//            if (next != null) {
//                executor.submit(() -> {
//                    try {
//                        log.info("LockRetryHandler: 대기중인 작업 실행");
//                        SecurityContextHolder.setContext(next.context);
//                        next.task.run();
//                        log.info("LockRetryHandler: 작업 실행 완료");
//                    } catch (Exception e) {
//                        log.error("LockRetryHandler: 작업 실행 중 오류 발생 - lockKey={}, errorMessage={}, stackTrace={}",
//                                lockKey, e.getMessage(), getStackTraceAsString(e));
//                    } finally {
//                        SecurityContextHolder.clearContext();
//                    }
//                });
//            } else {
//                waiters.remove(lockKey);
//            }
//        }
//    }
//
//    private String getStackTraceAsString(Throwable e) {
//        StringWriter sw = new StringWriter();
//        e.printStackTrace(new PrintWriter(sw));
//        return sw.toString();
//    }
//}
