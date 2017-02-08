package in.yagnyam.myid;

import com.googlecode.objectify.ObjectifyService;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

import in.yagnyam.myid.model.EntityNode;
import in.yagnyam.myid.model.GenesisNode;
import in.yagnyam.myid.model.RootNode;

public class StaticRegistrar {

    private static Boolean done = false;

    public static void register() {
        synchronized (done) {
            if (done) {
                return;
            }
            done = true;
        }
        ObjectifyService.register(EntityNode.class);
        ObjectifyService.register(GenesisNode.class);
        ObjectifyService.register(RootNode.class);
        Security.addProvider(new BouncyCastleProvider());
    }

}
