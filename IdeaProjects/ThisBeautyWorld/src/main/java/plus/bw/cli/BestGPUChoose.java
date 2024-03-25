package plus.bw.cli;

import com.jme3.opencl.DefaultPlatformChooser;
import com.jme3.opencl.Device;
import com.jme3.opencl.Platform;
import com.jme3.opencl.PlatformChooser;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class BestGPUChoose implements PlatformChooser {
    private static final Logger LOG = Logger.getLogger(DefaultPlatformChooser.class.getName());

    @Override
    public List<? extends Device> chooseDevices(List<? extends Platform> platforms) {
        ArrayList<Device> result = new ArrayList<>();
        for (Platform p : platforms) {
            if (!p.hasOpenGLInterop()) {
                continue; //must support interop
            }
            for (Device d : p.getDevices()) {
                if (d.hasOpenGLInterop() && d.getDeviceType()==Device.DeviceType.GPU) {
                    result.add(d); //GPU preferred
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        //no GPU devices found, try all
        for (Platform p : platforms) {
            if (!p.hasOpenGLInterop()) {
                continue; //must support interop
            }
            for (Device d : p.getDevices()) {
                if (d.hasOpenGLInterop()) {
                    result.add(d); //just interop needed
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        //still no one found, try without interop
        LOG.warning("No device with OpenCL-OpenGL-interop found, try without");
        for (Platform p : platforms) {
            for (Device d : p.getDevices()) {
                result.add(d);
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        //no devices available at all!
        return result; //result is empty
    }
}
