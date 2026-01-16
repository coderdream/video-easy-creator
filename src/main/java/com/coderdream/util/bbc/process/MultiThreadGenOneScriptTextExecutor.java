package com.coderdream.util.bbc.process;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.coderdream.entity.DownloadInfoEntity;
import com.coderdream.util.bbc.HtmlUtil;
import com.coderdream.util.cd.CdFileUtil;
import com.coderdream.util.file.PdfFileFinder;
import com.coderdream.util.pdf.ReadPdfUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author CoderDream
 */
@Slf4j
public class MultiThreadGenOneScriptTextExecutor {

    public static Integer POOL_SIZE = 50;

    public static List<DownloadInfoEntity> downloadInfoEntityList = Collections.synchronizedList(new ArrayList<>());

    private Integer corePoolSize = POOL_SIZE;

    private Integer maximumPoolSize = POOL_SIZE;

    private Integer keepAliveTime = 10;

    private static long startTime;

    private TimeUnit unit = TimeUnit.MILLISECONDS;

    private BlockingDeque workQueue = new LinkedBlockingDeque();

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        }
    };

    public ThreadPoolExecutor coreThreadPool = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            unit,
            workQueue,
            handler);

    public void initTestArr() {
        boolean test = true;

        List<DownloadInfoEntity> downloadInfoEntityListTemp = new ArrayList<>();
        if (test) {
            // 修改这里以处理你的PDF文件
            DownloadInfoEntity infoEntity = new DownloadInfoEntity();
            String ep = "251030"; // 使用你PDF文件的前缀

            log.info("========【参数化测试数据源初始化：读取todo.txt】========");
            String todoFilePath = CdFileUtil.getResourceRealPath() + File.separatorChar + "data" + File.separatorChar + "bbc" + File.separatorChar + "todo.txt";
            List<String> folderNames = FileUtil.readLines(todoFilePath, "UTF-8");
            if(CollectionUtil.isNotEmpty(folderNames)) {
                ep = folderNames.get(0);
            }
            String year = "20" + ep.substring(0, 2); // "2015"
            infoEntity.setFileUrl(
                ""); // URL可能不需要了，如果只是本地处理
            infoEntity.setPath(
                "D:/14_LearnEnglish/6MinuteEnglish/" + year + File.separator + ep + File.separator);
            infoEntity.setFileName(ep + ".html");
            downloadInfoEntityListTemp = List.of(infoEntity);
        } else {
            downloadInfoEntityListTemp = HtmlUtil.getDownloadHtmlInfo("txt", "2015");
        }

        for (DownloadInfoEntity downloadInfoEntity : downloadInfoEntityListTemp) {
            String fileName = downloadInfoEntity.getFileName();
            String realPdfName = PdfFileFinder.findPdfFileName(
                    fileName.substring(0, fileName.lastIndexOf(".")));
            downloadInfoEntity.setFileName(realPdfName);
            // 确保找到了PDF文件再添加
            if (realPdfName != null) {
                downloadInfoEntityList.add(downloadInfoEntity);
            }
        }
        log.info("Found {} files to process.", downloadInfoEntityList.size());
    }

    // 将 MyThread 改为实现 Runnable 的内部类，处理单个任务
    public class PdfProcessor implements Runnable {
        private final DownloadInfoEntity downloadInfoEntity;

        public PdfProcessor(DownloadInfoEntity downloadInfoEntity) {
            this.downloadInfoEntity = downloadInfoEntity;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            log.info("{} start processing {}", threadName, downloadInfoEntity.getFileName());
            long taskStartTime = System.currentTimeMillis();
            
            // 确保文件名不为空
            if (downloadInfoEntity.getFileName() == null || downloadInfoEntity.getFileName().length() < 6) {
                log.error("Invalid file name for processing: {}", downloadInfoEntity.getFileName());
                return;
            }
            
            // 核心处理逻辑
            ReadPdfUtil.genScriptTxt(downloadInfoEntity.getFileName().substring(0, 6),
                downloadInfoEntity.getFileName());
            
            long period = System.currentTimeMillis() - taskStartTime;
            log.info("{} finished in {} ms", threadName, period);
        }
    }

    public void printByMulThread() {
        for (DownloadInfoEntity entity : downloadInfoEntityList) {
            coreThreadPool.execute(new PdfProcessor(entity));
        }
        coreThreadPool.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        startTime = System.currentTimeMillis();
        MultiThreadGenOneScriptTextExecutor test = new MultiThreadGenOneScriptTextExecutor();
        test.initTestArr();
        test.printByMulThread();

        // 等待线程池执行完毕
        test.coreThreadPool.awaitTermination(1, TimeUnit.HOURS);
        long period = System.currentTimeMillis() - startTime;
        log.info("Total execution time: {} ms", period);
    }
}
