package net.minecraft.server;

import java.io.File;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.SERVER)
public class ServerEula {
    //private static final Logger LOG = LogManager.getLogger();
    //private final File eulaFile;
    //private final boolean acceptedEULA;

    public ServerEula(File eulaFile) {
        //this.eulaFile = eulaFile;
        //this.acceptedEULA = this.loadEULAFile(eulaFile);
    }


    private boolean loadEULAFile(File inFile) {
        return true;
//        FileInputStream fileinputstream = null;
//        boolean flag = false;
//
//        try {
//            Properties properties = new Properties();
//            fileinputstream = new FileInputStream(inFile);
//            properties.load(fileinputstream);
//            flag = Boolean.parseBoolean(properties.getProperty("eula", "false"));
//        }
//        catch (Exception var8) {
//            LOG.warn("Failed to load {}", (Object)inFile);
//            this.createEULAFile();
//        } finally {
//            IOUtils.closeQuietly((InputStream)fileinputstream);
//        }
//
//        return flag;
    }


    public boolean hasAcceptedEULA() {
        return true;
    }


    public void createEULAFile() {
//        FileOutputStream fileoutputstream = null;
//
//        try
//        {
//            Properties properties = new Properties();
//            //fileoutputstream = new FileOutputStream(this.eulaFile);
//            properties.setProperty("eula", "false");
//            properties.store(fileoutputstream, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
//        }
//        catch (Exception exception)
//        {
//            //LOG.warn("Failed to save {}", this.eulaFile, exception);
//        }
//        finally
//        {
//            IOUtils.closeQuietly((OutputStream)fileoutputstream);
//        }
    }
}