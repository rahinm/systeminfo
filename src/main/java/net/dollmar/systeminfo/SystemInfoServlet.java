package net.dollmar.systeminfo;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
Copyright 2017 Mohammad A. Rahin                                                                                                          

Licensed under the Apache License, Version 2.0 (the "License");                                                                           
you may not use this file except in compliance with the License.                                                                          
You may obtain a copy of the License at                                                                                                   

    http://www.apache.org/licenses/LICENSE-2.0                                                                                            

Unless required by applicable law or agreed to in writing, software                                                                       
distributed under the License is distributed on an "AS IS" BASIS,                                                                         
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.                                                                  
See the License for the specific language governing permissions and                                                                       
limitations under the License.       
 */

/**
 * The HTMl SystemInfo Controller servlet
 * 
 * @author Mohammad Rahin
 */
public class SystemInfoServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    private static final String VERSION = "0.8";

    private static final String FUNCTION_PARAM = "function";
    private static final String THREAD_ID_PARAM = "threadId";

    private static final String FUNCTION_HOME = "home";
    private static final String FUNCTION_HOSTINFO = "hostInfo";
    private static final String FUNCTION_JVMINFO = "jvmInfo";
    private static final String FUNCTION_PROPSINFO = "propsInfo";
    private static final String FUNCTION_CLASSLOADING_INFO = "classLoadingInfo";
    private static final String FUNCTION_MEMINFO = "memInfo";
    private static final String FUNCTION_HEAP_DUMP = "heapDump";
    private static final String FUNCTION_GCINFO = "gcInfo";
    private static final String FUNCTION_THREADSINFO = "threadsInfo";
    private static final String FUNCTION_SINGLE_THREADINFO = "singleThreadInfo";
    private static final String FUNCTION_THREAD_DUMP = "threadDump";

    public static final long MB = 1024 * 1024;


    public static class Metric {
        private String name;
        private String value;

        public Metric(String n, String v) {
            name = n;
            value = v;
        }


        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }


    public SystemInfoServlet() {
        //
    }

    public static String getVersion() {
        return VERSION;
    }


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }


    public void destroy() {
        //
    }


    private String currentDateTimeStamp(String format) {
        SimpleDateFormat sdf = new  SimpleDateFormat(format);

        return sdf.format(new Date());
    }


    private String convertToDateTimeStamp(long epochTime) {
        SimpleDateFormat sdf = new  SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

        return sdf.format(new Date(epochTime));
    }

    private String getHostName() throws IOException {
        return InetAddress.getLocalHost().getHostName();
    }

    private String getShortHostName() throws IOException {
        String hostName = getHostName();
        if (hostName.contains("."))
            return hostName.substring(0, hostName.indexOf("."));
        else    
            return hostName;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }	

    /*
     * A simple helper to call a MBean method 
     */
    private Object invokeMBeanMethod(ObjectName mxBean, String method, Object[] params, String[] sig) 
      throws InstanceNotFoundException, ReflectionException, MBeanException {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        return server.invoke(mxBean, "dumpHeap", params, sig);
    }
    
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String function = request.getParameter(FUNCTION_PARAM);
        String threadId = request.getParameter(THREAD_ID_PARAM);

        if (function == null) function = FUNCTION_HOME;

        if (function.equals(FUNCTION_HOME)) displayHomePage(request, response);
        else if (function.equals(FUNCTION_HOSTINFO)) displayHostInfo(request, response);
        else if (function.equals(FUNCTION_JVMINFO)) displayJvmInfo(request, response);
        else if (function.equals(FUNCTION_PROPSINFO)) displayPropertiesInfo(request, response);
        else if (function.equals(FUNCTION_CLASSLOADING_INFO)) displayClassLoadingInfo(request, response);
        else if (function.equals(FUNCTION_MEMINFO)) displayMemoryInfo(request, response);
        else if (function.equals(FUNCTION_HEAP_DUMP)) doHeapDump(request, response);
        else if (function.equals(FUNCTION_GCINFO)) displayGcInfo(request, response);
        else if (function.equals(FUNCTION_THREADSINFO)) displayThreadsInfo(request, response);
        else if (function.equals(FUNCTION_SINGLE_THREADINFO)) displaySingleThreadInfo(request, response, threadId);
        else if (function.equals(FUNCTION_THREAD_DUMP)) doThreadDump(request, response);
        else {
            PrintWriter out = response.getWriter();
            out.print(buildHtmlPage("Error", String.format("Invalid function \"%s\".", function)));      
        }
    }


    private String buildHtmlPage(final String title, final String content) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/SystemInfo.css\"/>");
        sb.append("</head>");
        sb.append("<body bgcolor=\"#b8d6ca\">");
        sb.append(String.format("<h1>%s</h1>", title));
        sb.append(content);
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }


    private static final String HOME_PAGE_INFO = "SystemInfo is a collection of JVM tools packaged together within a war file.<br>" + 
            "    This war file should be deployed into a JEE container like Tomcat, JBoss etc.<br>" + 
            "    to gather JVM run-time information on Memory usage, GC details and threads<br>" + 
            "    details." + 
            "    <br><br>" + 
            "    The following set of information can be obtained.\n" + 
            "    <ul>\n" + 
            "      <li><a href='SystemInfo?function=hostInfo'>Host Info</li>\n" + 
            "      <li><a href='SystemInfo?function=jvmInfo'>JVM Info</li>\n" + 
            "      <li><a href='SystemInfo?function=propsInfo'>Properties Info</li>\n" + 
            "      <li><a href='SystemInfo?function=classLoadingInfo'>ClassLoading Info</li>\n" + 
            "      <li><a href='SystemInfo?function=memInfo'>Memory Info</li>\n" + 
            "      <li><a href='SystemInfo?function=gcInfo'>GC Info</li>\n" + 
            "      <li><a href='SystemInfo?function=threadsInfo'>Threads Info</li>\n" + 
            "    </ul>";

    private void displayHomePage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(HOME_PAGE_INFO);

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("Home", sb.toString()));
    }


    public static boolean isGetter(Method method){
        if(!method.getName().startsWith("get"))      return false;
        if(method.getParameterTypes().length != 0)   return false;  
        if(void.class.equals(method.getReturnType())) return false;

        return true;
    }


    private String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                        ),
                " "
                );
    }


    private static final String EXCEPTION_METRICS = "Arch System Load Average Version Object Name Available Processors Name Class";

    private ArrayList<Metric> getAdditionalHostMetrics(Object obj) {
        ArrayList<Metric> metrics = new ArrayList<>();

        Method[] methods = obj.getClass().getMethods();

        String name;
        String value;
        for (Method method : methods) {
            if (isGetter(method)) {
                name = splitCamelCase(method.getName().replaceAll("get", ""));
                if (EXCEPTION_METRICS.contains(name)) break;
                method.setAccessible(true); 
                try {
                    value = (String) method.invoke(obj).toString();
                }
                catch (Exception e) {
                    value = "";
                }
                metrics.add(new Metric(name, value));
            }
        }
        return metrics;
    }



    private void displayHostInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {


        StringBuilder sb = new StringBuilder();

        sb.append("<table border='1'>");

        OperatingSystemMXBean osInfo = ManagementFactory.getOperatingSystemMXBean();
        File rootDir = new File("/");

        sb.append("<tr><td>Host Name</td><td>" + getHostName() + "</td></tr>");
        sb.append("<tr><td>OS Name</td><td>" + osInfo.getName() + "</td></tr>");
        sb.append("<tr><td>OS Version</td><td>" + osInfo.getVersion() + "</td></tr>");
        sb.append("<tr><td>Architecture</td><td>" + osInfo.getArch() + "</td></tr>");
        sb.append("<tr><td>Processor Count</td><td>" + osInfo.getAvailableProcessors() + "</td></tr>");
        sb.append("<tr><td>System Load Average</td><td>" + osInfo.getSystemLoadAverage() + "</td></tr>");

        ArrayList<Metric> xtraData = getAdditionalHostMetrics(osInfo);
        for (Metric m : xtraData) {
            sb.append(String.format("<tr><td>%s</td><td>%s</td></tr>", m.getName(), m.getValue()));
        }

        sb.append("<tr><td>Total Disk Space (MB)</td><td>" + rootDir.getTotalSpace() / MB + "</td></tr>");
        sb.append("<tr><td>Free Disk Space (MB)</td><td>" + rootDir.getFreeSpace() / MB + "</td></tr>");

        sb.append("</table>");

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("Host Info", sb.toString()));
    }


    private void displayJvmInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        RuntimeMXBean jvmInfo = ManagementFactory.getRuntimeMXBean();

        StringBuilder jvmArgs = new StringBuilder();
        for (String a : jvmInfo.getInputArguments()) {
          jvmArgs.append(a).append(' ');
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<table border='1'>");

        sb.append("<tr><td>JVM Name</td><td>" + jvmInfo.getName() + "</td></tr>");
        sb.append("<tr><td>JVM Implementation Name</td><td>" + jvmInfo.getVmName() + "</td></tr>");
        sb.append("<tr><td>JVM Vendor</td><td>" + jvmInfo.getVmVendor() + "</td></tr>");
        sb.append("<tr><td>JVM Version</td><td>" + jvmInfo.getVmVersion() + "</td></tr>");
        sb.append("<tr><td>JVM Specifications Name</td><td>" + jvmInfo.getSpecName() + "</td></tr>");
        sb.append("<tr><td>JVM Specifications Version</td><td>" + jvmInfo.getSpecVersion() + "</td></tr>");
        sb.append("<tr><td>JVM Specifications Vendor</td><td>" + jvmInfo.getSpecVendor() + "</td></tr>");
        sb.append("<tr><td>JVM Start Time</td><td>" + convertToDateTimeStamp(jvmInfo.getStartTime()) + "</td></tr>");
        sb.append("<tr><td>JVM Uptime (seconds)</td><td>" + jvmInfo.getUptime()/1000 + "</td></tr>");
        sb.append("<tr><td>JVM Classpath</td><td>" + jvmInfo.getClassPath() + "</td></tr>");
        sb.append("<tr><td>JVM Library Path</td><td>" + jvmInfo.getLibraryPath() + "</td></tr>");
        sb.append("<tr><td>JVM Boot Classpath</td><td>" + jvmInfo.getBootClassPath() + "</td></tr>");
        sb.append("<tr><td>JVM Arguments</td><td>" + jvmArgs.toString() + "</td></tr>");

        sb.append("</table>");

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("JVM Info", sb.toString()));
    }


    private void displayClassLoadingInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        ClassLoadingMXBean clInfo = ManagementFactory.getClassLoadingMXBean();

        StringBuilder sb = new StringBuilder();

        sb.append("<table border='1'>");

        sb.append("<tr><td>Loaded Class Count</td><td>" + clInfo.getLoadedClassCount() + "</td></tr>");
        sb.append("<tr><td>Total Loaded Class Count</td><td>" + clInfo.getTotalLoadedClassCount() + "</td></tr>");
        sb.append("<tr><td>Unloaded Class Count</td><td>" + clInfo.getUnloadedClassCount() + "</td></tr>");


        sb.append("</table>");

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("ClassLoading Info", sb.toString()));
    }


    private void displayPropertiesInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        RuntimeMXBean jvmInfo = ManagementFactory.getRuntimeMXBean();

        Map<String, String> props = jvmInfo.getSystemProperties();
        SortedSet<String> propNames = new TreeSet<String>(props.keySet());

        StringBuilder sb = new StringBuilder();

        sb.append("<table border='1'>");
        sb.append("<tr><th>Property Name</th>");
        sb.append("<th>Property Value</th>");

        for (String name: propNames) {
            sb.append("<tr><td>").append(name).append("</td>");
            sb.append("<td>").append(props.get(name)).append("</td></tr>");      
        }

        sb.append("</table>");

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("Properties Info", sb.toString()));
    }


    private void displayMemoryInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        MemoryMXBean memInfo = ManagementFactory.getMemoryMXBean();

        StringBuilder sb = new StringBuilder();

        MemoryUsage heapInfo = memInfo.getHeapMemoryUsage(); 
        sb.append("<h2>Heap memory usage</h2>");
        sb.append("<table border='1'>");
        sb.append("<tr><td>Initial Memory (MB)</td><td>"   + heapInfo.getInit() / MB + "</td></tr>");
        sb.append("<tr><td>Maximum Memory (MB)</td><td>"   + heapInfo.getMax()  / MB + "</td></tr>");
        sb.append("<tr><td>Committed Memory (MB)</td><td>" + heapInfo.getCommitted() / MB + "</td></tr>");
        sb.append("<tr><td>Used Memory (MB)</td><td>"      + heapInfo.getUsed()  / MB + "</td></tr>");
        sb.append("</table>");  

        sb.append("<br>");
        String heapDump = createButtonForPopup(FUNCTION_HEAP_DUMP, "noparam", "", "<h3><i>Get Heap Dump</i></h3>");
        String refresh = String.format("<a href=\"SystemInfo?function=%s\"><button><h3><i>Refresh Screen</i></h3></button></a>", FUNCTION_MEMINFO);
        sb.append("<table border='0' cellpadding='5'>");
        sb.append("<tr><td>").append(heapDump).append("</td><td>             </td><td>").append(refresh).append("</td></tr>");
        sb.append("</table>");

        sb.append("<br>");

        MemoryUsage nonHeapInfo = memInfo.getNonHeapMemoryUsage(); 
        sb.append("<h2>Non-heap memory usage</h2>");
        sb.append("<table border='1'>");
        sb.append("<tr><td>Initial Memory (MB)</td><td>"   + nonHeapInfo.getInit() / MB + "</td></tr>");
        sb.append("<tr><td>Maximum Memory (MB)</td><td>"   + nonHeapInfo.getMax() / MB + "</td></tr>");
        sb.append("<tr><td>Committed Memory (MB)</td><td>" + nonHeapInfo.getCommitted() / MB + "</td></tr>");
        sb.append("<tr><td>Used Memory (MB)</td><td>"      + nonHeapInfo.getUsed() / MB + "</td></tr>");
        sb.append("</table>");  


        List<MemoryPoolMXBean> memoryPoolList = ManagementFactory.getMemoryPoolMXBeans();
        sb.append("<h2>Memory pools info</h2>");
        sb.append("<table border='1'>");
        sb.append("<tr> <th>Name</th><th>Usage</th> <th>Collection Usage</th>");
        sb.append("     <th>Peak Usage</th> <th>Type</th>  <th>Memory Manager Names</th></tr>");

        for (MemoryPoolMXBean m : memoryPoolList) {
            sb.append("<tr>");
            sb.append(String.format("<td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>", 
                    m.getName(), m.getUsage(), m.getCollectionUsage(), m.getPeakUsage(), m.getType()));
            sb.append("<td>");
            String[] memManagerNames = m.getMemoryManagerNames();
            for (String mmn : memManagerNames) {
                sb.append(mmn + ", ");
            }
            sb.append("</td></tr>");
        }
        sb.append("</table>");


        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("Memory Info", sb.toString()));
    }


    private void displayGcInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();

        StringBuilder sb = new StringBuilder();


        sb.append("<table border='1'>");
        sb.append("<tr> <th>Name</th>            <th>Collection Count</th>");
        sb.append("     <th>Collection Time (in seconds)</th> <th>Memory Pools</th></tr>");

        for (GarbageCollectorMXBean gc : gcList) {
            sb.append("<tr>");
            sb.append(String.format("<td>%s</td><td>%d</td><td>%f</td>",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime()/1000.0));

            sb.append("<td>");
            String[] memPoolNames = gc.getMemoryPoolNames();
            if (memPoolNames != null) {

                for (String mpn : memPoolNames) {
                    sb.append(mpn + ", ");
                }
            }
            sb.append("</td></tr>");
        }
        sb.append("</table>");

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("GC Info", sb.toString()));
    }


    private void displayThreadsInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        int currentThreadCount = mxBean.getThreadCount();

        List<ThreadInfo> newThreads = new ArrayList<ThreadInfo>();
        List<ThreadInfo> runnableThreads = new ArrayList<ThreadInfo>();
        List<ThreadInfo> blockedThreads = new ArrayList<ThreadInfo>();
        List<ThreadInfo> waitingThreads = new ArrayList<ThreadInfo>();
        List<ThreadInfo> timedWaitingThreads = new ArrayList<ThreadInfo>();
        List<ThreadInfo> terminatedThreads = new ArrayList<ThreadInfo>();

        ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds());
        for (ThreadInfo ti : threadInfos) {
            switch (ti.getThreadState()) {
            case NEW: newThreads.add(ti); break;
            case RUNNABLE: runnableThreads.add(ti); break;
            case BLOCKED: blockedThreads.add(ti); break;
            case WAITING: waitingThreads.add(ti); break;
            case TIMED_WAITING: timedWaitingThreads.add(ti); break;
            case TERMINATED: terminatedThreads.add(ti); break;
            }
        }
        long[] deadlockedThreads = mxBean.findDeadlockedThreads();

        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");

        sb.append("<tr><td>Current number of all live threads</td><td>" + currentThreadCount + "</td></tr>");
        sb.append("<tr><td>Current number of daemon threads</td><td>" + mxBean.getDaemonThreadCount() + "</td></tr>");
        sb.append("<tr><td>Total number of threads started</td><td>" + mxBean.getTotalStartedThreadCount() + "</td></tr>");
        sb.append("<tr><td>Peak number of live threads</td><td>" + mxBean.getPeakThreadCount() + "</td></tr>");
        sb.append("<tr><td>Number of threads in deadlock state</td><td>" 
                + ((deadlockedThreads != null) ? deadlockedThreads.length : 0)+ "</td></tr>");
        if (deadlockedThreads != null) {
            sb.append("<tr><td>Deadlocked thread ids </td>");
            sb.append("<td>");

            for (long id : deadlockedThreads) {
                sb.append(id + ", ");
            }
            sb.append("</td></tr>");
        }
        sb.append("<tr><td>Number of threads in 'NEW' state</td><td>" + newThreads.size() + "</td></tr>");
        sb.append("<tr><td>Number of threads in 'RUNNABLE' state</td><td>" + runnableThreads.size() + "</td></tr>");
        sb.append("<tr><td>Number of threads in 'BLOCKED' state</td><td>" + blockedThreads.size() + "</td></tr>");
        sb.append("<tr><td>Number of threads in 'WAITING' state</td><td>" + waitingThreads.size() + "</td></tr>");
        sb.append("<tr><td>Number of threads in 'TIMED_WAITING' state</td><td>" + timedWaitingThreads.size() + "</td></tr>");
        sb.append("<tr><td>Number of threads in 'TERMINATED' state</td><td>" + terminatedThreads.size() + "</td></tr>");

        sb.append("</table>");

        //    sb.append(createLinkForPopup(FUNCTION_THREAD_DUMP, "noparam", "", 
        //        "<h3><i>Click to create a thread dump file</i></h3>"));

        sb.append("<br>");

        String thDump = createButtonForPopup(FUNCTION_THREAD_DUMP, "noparam", "", "<h3><i>Get Thread Dump</i></h3>");
        String refresh = String.format("<a href=\"SystemInfo?function=%s\"><button><h3><i>Refresh Screen</i></h3></button></a>", FUNCTION_THREADSINFO);
        sb.append("<table border='0' cellpadding='5'>");
        sb.append("<tr><td>").append(thDump).append("</td><td>             </td><td>").append(refresh).append("</td></tr>");
        sb.append("</table>");

        sb.append("<br>");

        sb.append(buildHtmlTableForThreads("Threads: BLOCKED", blockedThreads, currentThreadCount));
        sb.append(buildHtmlTableForThreads("Threads: WAITING", waitingThreads, currentThreadCount));
        sb.append(buildHtmlTableForThreads("Threads: TIMED_WAITING", timedWaitingThreads, currentThreadCount ));
        sb.append(buildHtmlTableForThreads("Threads: RUNNABLE", runnableThreads, currentThreadCount));
        sb.append(buildHtmlTableForThreads("Threads: NEW", newThreads, currentThreadCount));
        sb.append(buildHtmlTableForThreads("Threads: TERMINATED", terminatedThreads, currentThreadCount));


        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("Threads Info", sb.toString()));
    }



    private String buildHtmlTableForThreads(String title, List<ThreadInfo> threadList, int currentThreadCount) {
        if (threadList == null || threadList.size() == 0)
            return "";

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("<h2>%s [%d - %02.1f%%]</h2>", title, threadList.size(), 
                (100 * (float) threadList.size() / currentThreadCount)));

        sb.append("<table border='1'>");
        sb.append("<tr><th>Thread Id</th>");
        sb.append("<th>Thread Name</th>");
        sb.append("<th>Thread State</th>");
        sb.append("<th>Blocked Count</th>");
        sb.append("<th>Blocked Time</th>");
        sb.append("<th>Waited Count</th>");
        sb.append("<th>Waited Time </th></tr>");

        for (ThreadInfo ti : threadList) {
            //sb.append("<tr><td>").append(ti.getThreadId()).append("</td>");
            String tid = "" + ti.getThreadId();
            sb.append("<tr><td>").append(createLinkForPopup(FUNCTION_SINGLE_THREADINFO, THREAD_ID_PARAM, tid, tid)).append("</td>");
            sb.append("<td>").append(ti.getThreadName()).append("</td>");
            sb.append("<td>").append(ti.getThreadState().toString()).append("</td>");
            sb.append("<td>").append(ti.getBlockedCount()).append("</td>");
            sb.append("<td>").append(ti.getBlockedTime()).append("</td>");
            sb.append("<td>").append(ti.getWaitedCount()).append("</td>");
            sb.append("<td>").append(ti.getWaitedTime()).append("</td></tr>");
        }

        sb.append("</table>");
        sb.append("<br>");

        return sb.toString();
    }

    private String createLinkForPopup(String function, String param, String value, String linkText) {
        String url = String.format("SystemInfo?function=%s&%s=%s", function, param, value);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<a href=\"%s\" ", url)).append("target=\"popup\" ");
        sb.append(
                String.format("onclick=\"window.open('%s', 'popup', 'width=1000, height=600'); return false;\">", url));
        sb.append(linkText).append("</a>");
        return sb.toString();
    }

    private String createButtonForPopup(String function, String param, String value, String linkText) {
        String url = String.format("SystemInfo?function=%s&%s=%s", function, param, value);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<a href=\"%s\" ", url)).append("target=\"popup\" ");
        sb.append(
                String.format("onclick=\"window.open('%s', 'popup', 'width=1000, height=600'); return false;\">", url));
        sb.append("<button>").append(linkText).append("</button></a>");
        return sb.toString();
    }


    private void displaySingleThreadInfo(HttpServletRequest request, HttpServletResponse response, String threadId) 
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        String pageHeader = "Single Thread Info";

        if (threadId == null || threadId.isEmpty()) {
            pageHeader = "Error";
            sb.append(String.format("Thread identifier not specified."));
        }
        else {
            int tid = Integer.parseInt(threadId);
            ThreadMXBean threadsInfo = ManagementFactory.getThreadMXBean();
            ThreadInfo ti = threadsInfo.getThreadInfo(tid, 50);

            if (ti == null) {
                pageHeader = "Error";
                sb.append(String.format("No information about this thread (id=%d) available.", tid));
            }
            else {
                sb.append("<table border='1'>");
                sb.append("<tr><td>Thread id</td><td>" + ti.getThreadId() + "</td></tr>");
                sb.append("<tr><td>Thread name</td><td>" + ti.getThreadName() + "</td></tr>");
                sb.append("<tr><td>Thread state</td><td>" + ti.getThreadState().toString() + "</td></tr>");
                sb.append("<tr><td>Thread CPU Time (in ns)</td><td>" + threadsInfo.getThreadCpuTime(ti.getThreadId()) + "</td></tr>");
                sb.append("<tr><td>Thread User Time (in ns)</td><td>" + threadsInfo.getThreadUserTime(ti.getThreadId()) + "</td></tr>");
                sb.append("<tr><td>Total blocked count</td><td>" + ti.getBlockedCount() + "</td></tr>");
                sb.append("<tr><td>Total blocked time (in seconds) </td><td>" + ti.getBlockedTime() / 1000.0 + "</td></tr>");
                sb.append("<tr><td>Total waited count</td><td>" + ti.getWaitedCount() + "</td></tr>");
                sb.append("<tr><td>Total waited time (in seconds) </td><td>" + ti.getWaitedTime() / 1000.0 + "</td></tr>");
                sb.append("<tr><td>Lock name </td><td>" + ti.getLockName() + "</td></tr>");
                sb.append("<tr><td>Lock owning thread id </td><td>" + ti.getLockOwnerId() + "</td></tr>");

                LockInfo li = ti.getLockInfo();
                if (li != null) {
                    sb.append("<tr><td>Lock owning class name </td><td>" + li.getClassName() + "</td></tr>");

                }
                sb.append("</table>");

                sb.append("<br>");

                sb.append("<h2>Stack Trace: </h2>");
                for (StackTraceElement ste : ti.getStackTrace()) {
                    sb.append(ste.toString()).append("\n");
                }   
            }
        }
        sb.append("<br>").append("<h2>  </h2>");
        sb.append("<a href='JavaScript:window.close()'><button><h3><i>Close Window</i></h3></button></a>");

        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage(pageHeader, sb.toString()));
    }


    private void doThreadDump(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        StringBuilder dump = new StringBuilder();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds(), 100);

        for (ThreadInfo ti : threadInfos) {
            dump.append('"');
            dump.append(ti.getThreadName());
            dump.append("\" ");
            Thread.State state = ti.getThreadState();
            dump.append("\n   java.lang.Thread.State: ");
            dump.append(state);
            StackTraceElement[] stes = ti.getStackTrace();
            for (StackTraceElement ste : stes) {
                dump.append("\n      at ");
                dump.append(ste);
            }
            dump.append("\n\n");
        }

        String outFile = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            outFile = System.getenv("Temp") 
                    + String.format("%s%s-ThreadDump-%s.txt", System.getProperty("file.separator"), getShortHostName(), this.currentDateTimeStamp("yyyyMMddHHmmss"));
        }
        else {
            outFile = String.format("/tmp/%s-ThreadDump-%s.txt", getShortHostName(), this.currentDateTimeStamp("yyyyMMddHHmmss"));
        }
        if (outFile != null) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))) {
                writer.write(dump.toString());
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>Thread dump saved in file: " + outFile + "</h3");
        sb.append("<br>").append("<h2>  </h2>");
        sb.append("<a href='JavaScript:window.close()'><button><h3><i>Close Window</i></h3></button></a>");
        
        PrintWriter out = response.getWriter();
        out.print(buildHtmlPage("Thread Dump", sb.toString()));
    }


    @SuppressWarnings("restriction")
    private void doHeapDump(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String outFile = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            outFile = System.getenv("Temp") 
                    + String.format("%s%s-HeapDump-%s.hprof", System.getProperty("file.separator"), getShortHostName(), this.currentDateTimeStamp("yyyyMMddHHmmss"));
        }
        else {
            outFile = String.format("/tmp/%s-HeapDump-%s.hprof", getShortHostName(), this.currentDateTimeStamp("yyyyMMddHHmmss"));
        }
        if (outFile != null) {
            try {
                ObjectName mxBean = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
                Object[] params = new Object[] {outFile, Boolean.FALSE};
                String[] sigs = new String[] { String.class.getName(), boolean.class.getName() };

                invokeMBeanMethod(mxBean, "dumpHeap", params, sigs);
                
                StringBuilder sb = new StringBuilder();
                sb.append("<h3>Heap dump saved in file: " + outFile + "</h3");
                sb.append("<br>").append("<h2>  </h2>");
                sb.append("<a href='JavaScript:window.close()'><button><h3><i>Close Window</i></h3></button></a>");
                
                response.getWriter().print(buildHtmlPage("Heap Dump", sb.toString()));                
            }
            catch (Exception e) {
                throw new IOException("Error: Failed to create heap dump file", e);
            }
        }
    }      
}

