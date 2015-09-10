package gr.iti.openzoo.service.impl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class ServerStatisticsImpl {
    
    private com.sun.management.OperatingSystemMXBean sunosmxb;
    private MemoryMXBean mmxb;
    private long lastSystemTime;
    private long lastProcessCpuTime;
    
    // Info
    private String arch;
    private String name;
    private String version;
    
    // CPU
    private int  availableProcessors;
    private double cpuUsage;            // [0,1]
    private double processCpuLoad;      // [0,1]
    private double systemCpuLoad;       // [0,1]
    
    // Memory
    private long physicalFree;          // bytes
    private long physicalTotal;         // bytes
    private long swapFree;              // bytes
    private long swapTotal;             // bytes
    private long committedVMSize;       // bytes
    private MemoryUsage heapMemoryUsage;// bytes
    private MemoryUsage nonHeapMemoryUsage;// bytes
    
    // Space
    private long spaceFree;             // bytes
    private long spaceTotal;            // bytes
    
    public ServerStatisticsImpl()
    {        
        sunosmxb = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        mmxb = ManagementFactory.getMemoryMXBean();
        lastSystemTime = System.nanoTime();
        lastProcessCpuTime = sunosmxb.getProcessCpuTime();
        
        arch = sunosmxb.getArch();
        name = sunosmxb.getName();
        version = sunosmxb.getVersion();
        
        availableProcessors = sunosmxb.getAvailableProcessors();
        
    }

    private synchronized void update()
    {
        long systemTime     = System.nanoTime();
        long processCpuTime = sunosmxb.getProcessCpuTime();
        cpuUsage = (double) ( processCpuTime - lastProcessCpuTime ) / ( systemTime - lastSystemTime ) / availableProcessors;
        lastSystemTime     = systemTime;
        lastProcessCpuTime = processCpuTime;
        processCpuLoad = sunosmxb.getProcessCpuLoad();
        systemCpuLoad = sunosmxb.getSystemCpuLoad();
        
        physicalFree = sunosmxb.getFreePhysicalMemorySize();
        physicalTotal = sunosmxb.getTotalPhysicalMemorySize();
        swapFree = sunosmxb.getFreeSwapSpaceSize();
        swapTotal = sunosmxb.getTotalSwapSpaceSize();
        committedVMSize = sunosmxb.getCommittedVirtualMemorySize();
        
        heapMemoryUsage = mmxb.getHeapMemoryUsage();
        nonHeapMemoryUsage = mmxb.getNonHeapMemoryUsage();
        
        spaceFree = (new File(".")).getFreeSpace();
        spaceTotal = (new File(".")).getTotalSpace();
    }
    
    public synchronized JSONObject get()
    {
        update();
        
        JSONObject json = new JSONObject();
        try
        {
            json.put("arch", arch);
            json.put("name", name);
            json.put("version", version);
            
            JSONObject cpu = new JSONObject();
            cpu.put("processors", availableProcessors);
            cpu.put("cpuUsage", cpuUsage);
            cpu.put("processCpuLoad", processCpuLoad);
            cpu.put("systemCpuLoad", systemCpuLoad);
            json.put("cpu", cpu);
            
            JSONObject mem = new JSONObject();
            mem.put("physicalFree", physicalFree);
            mem.put("physicalTotal", physicalTotal);
            mem.put("swapFree", swapFree);
            mem.put("swapTotal", swapTotal);
            mem.put("committedVMSize", committedVMSize);
            JSONObject heap = new JSONObject();
            heap.put("init", heapMemoryUsage.getInit());
            heap.put("used", heapMemoryUsage.getUsed());
            heap.put("committed", heapMemoryUsage.getCommitted());
            heap.put("max", heapMemoryUsage.getMax());
            mem.put("heap", heap);
            JSONObject nonheap = new JSONObject();
            nonheap.put("init", nonHeapMemoryUsage.getInit());
            nonheap.put("used", nonHeapMemoryUsage.getUsed());
            nonheap.put("committed", nonHeapMemoryUsage.getCommitted());
            nonheap.put("max", nonHeapMemoryUsage.getMax());
            mem.put("nonheap", nonheap);
            json.put("mem", mem);
            
            JSONObject space = new JSONObject();
            space.put("free", spaceFree);
            space.put("total", spaceTotal);
            json.put("space", space);
        }
        catch (JSONException e)
        {
            System.err.println("JSONException in get: " + e);
        }
        
        return json;
    }
}
