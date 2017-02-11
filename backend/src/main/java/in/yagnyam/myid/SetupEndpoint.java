/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package in.yagnyam.myid;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.List;

import in.yagnyam.myid.model.EntityNode;
import in.yagnyam.myid.model.GenesisNode;
import in.yagnyam.myid.model.RootNode;
import in.yagnyam.myid.utils.PemUtils;
import in.yagnyam.myid.utils.SignUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Api(
        name = "setupApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "myid.yagnyam.in",
                ownerName = "myid.yagnyam.in",
                packagePath = ""
        )
)
@Slf4j
public class SetupEndpoint {

    static {
        StaticRegistrar.register();
    }

    @ApiMethod(name = "setup", httpMethod = ApiMethod.HttpMethod.POST)
    public List<EntityNode> setup() {
        log.info("setup");
        GenesisNode genesisNode = genesisNode();
        // Node for NL
        RootNode nlNode = rootNode("NL", genesisNode);
        RootNode inNode = rootNode("IN", genesisNode);
        // User nodes under NL
        //EntityNode annekeNode = personNode("Anneke", "112682765", nlNode);
        //EntityNode bobNode = personNode("Bob", "212682776", nlNode);
        // User nodes under IN
        //EntityNode chennaNode = personNode("Chenna", "322682765", inNode);
        //EntityNode dunnaNode = personNode("Dunna", "432682776", inNode);
        // College nodes under NL
        EntityNode universityNode = universityNode("AMU", nlNode);
        // Degree Certification Node
        EntityNode policeNode = policeNode("Cop", nlNode);
        return Arrays.asList(genesisNode, nlNode, inNode, universityNode, policeNode);
    }

    @SneakyThrows
    private GenesisNode genesisNode() {
        GenesisNode genesisNode = new GenesisNode();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair genesisKeyPair = generator.generateKeyPair();
        genesisNode.setPath("/");
        genesisNode.setKeyPair(genesisKeyPair);
        genesisNode.setPrivateKey(PemUtils.encodePrivateKey(genesisKeyPair.getPrivate()));
        genesisNode.setVerificationKey(PemUtils.encodePublicKey(genesisKeyPair.getPublic()));
        genesisNode.setDescription("Genesis Node");
        // Add self signature
        genesisNode.setSigner("/");
        genesisNode.setSignatureSha256(SignUtils.getSignature(genesisNode.contentToSign(), SignUtils.ALGORITHM_SHA256WithRSA, genesisKeyPair.getPrivate()));
        genesisNode.setSignatureMd5(SignUtils.getSignature(genesisNode.contentToSign(), SignUtils.ALGORITHM_MD5WithRSA, genesisKeyPair.getPrivate()));
        // Add genesis node
        return (GenesisNode) NodeEndpoint.newNode(genesisNode);
    }

    @SneakyThrows
    private RootNode rootNode(String name, GenesisNode signer) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        RootNode rootNode = new RootNode();
        rootNode.setPath("/root/" + name.toLowerCase());
        rootNode.setKeyPair(keyPair);
        rootNode.setPrivateKey(PemUtils.encodePrivateKey(keyPair.getPrivate()));
        rootNode.setVerificationKey(PemUtils.encodePublicKey(keyPair.getPublic()));
        rootNode.setDescription("Root node for " + name);
        // Add signature by Genesis Node
        rootNode.setSigner(signer.getPath());
        rootNode.setSignatureSha256(SignUtils.getSignature(rootNode.contentToSign(), SignUtils.ALGORITHM_SHA256WithRSA, signer.getKeyPair().getPrivate()));
        rootNode.setSignatureMd5(SignUtils.getSignature(rootNode.contentToSign(), SignUtils.ALGORITHM_MD5WithRSA, signer.getKeyPair().getPrivate()));
        return  (RootNode) NodeEndpoint.newNode(rootNode);
    }

    @SneakyThrows
    private EntityNode personNode(String name, String bsn, RootNode signer) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        EntityNode personNode = new EntityNode();
        personNode.setPath("/person/" + name.toLowerCase());
        personNode.setKeyPair(keyPair);
        personNode.setPrivateKey(PemUtils.encodePrivateKey(keyPair.getPrivate()));
        personNode.setVerificationKey(PemUtils.encodePublicKey(keyPair.getPublic()));
        personNode.setDescription("Node for " + name + ". Hash is for BSN: " + bsn);
        personNode.setDataHashSha256(SignUtils.getHash(bsn, SignUtils.ALGORITHM_SHA256));
        personNode.setDataHashMd5(SignUtils.getHash(bsn, SignUtils.ALGORITHM_MD5));
        // Add signature by Genesis Node
        personNode.setSigner(signer.getPath());
        personNode.setSignatureSha256(SignUtils.getSignature(personNode.contentToSign(), SignUtils.ALGORITHM_SHA256WithRSA, signer.getKeyPair().getPrivate()));
        personNode.setSignatureMd5(SignUtils.getSignature(personNode.contentToSign(), SignUtils.ALGORITHM_MD5WithRSA, signer.getKeyPair().getPrivate()));
        return NodeEndpoint.newNode(personNode);
    }

    @SneakyThrows
    private EntityNode policeNode(String name, RootNode signer) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        EntityNode collegeNode = new EntityNode();
        collegeNode.setPath("/police/" + name.toLowerCase());
        collegeNode.setKeyPair(keyPair);
        collegeNode.setPrivateKey(PemUtils.encodePrivateKey(keyPair.getPrivate()));
        collegeNode.setVerificationKey(PemUtils.encodePublicKey(keyPair.getPublic()));
        collegeNode.setDescription("Node for Police " + name + ".");
        // Add signature by Root Node
        collegeNode.setSigner(signer.getPath());
        collegeNode.setSignatureSha256(SignUtils.getSignature(collegeNode.contentToSign(), SignUtils.ALGORITHM_SHA256WithRSA, signer.getKeyPair().getPrivate()));
        collegeNode.setSignatureMd5(SignUtils.getSignature(collegeNode.contentToSign(), SignUtils.ALGORITHM_MD5WithRSA, signer.getKeyPair().getPrivate()));
        return NodeEndpoint.newNode(collegeNode);
    }


    @SneakyThrows
    private EntityNode universityNode(String name, RootNode signer) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        EntityNode collegeNode = new EntityNode();
        collegeNode.setPath("/university/" + name.toLowerCase());
        collegeNode.setKeyPair(keyPair);
        collegeNode.setPrivateKey(PemUtils.encodePrivateKey(keyPair.getPrivate()));
        collegeNode.setVerificationKey(PemUtils.encodePublicKey(keyPair.getPublic()));
        collegeNode.setDescription("Node for University " + name + ".");
        // Add signature by Root Node
        collegeNode.setSigner(signer.getPath());
        collegeNode.setSignatureSha256(SignUtils.getSignature(collegeNode.contentToSign(), SignUtils.ALGORITHM_SHA256WithRSA, signer.getKeyPair().getPrivate()));
        collegeNode.setSignatureMd5(SignUtils.getSignature(collegeNode.contentToSign(), SignUtils.ALGORITHM_MD5WithRSA, signer.getKeyPair().getPrivate()));
        return NodeEndpoint.newNode(collegeNode);
    }


    /*
    @SneakyThrows
    EntityNode certificateNode(String name, String content, RootNode signer) {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair keyPair = generator.generateKeyPair();
        EntityNode certNode = new EntityNode();
        certNode.setPath("/college/" + name.toLowerCase());
        certNode.setKeyPair(keyPair);
        certNode.setPrivateKey(PemUtils.encodePrivateKey(keyPair.getPrivate()));
        certNode.setVerificationKey(PemUtils.encodePublicKey(keyPair.getPublic()));
        certNode.setDescription("Node for College " + name + ".");
        // Add signature by Root Node
        certNode.setSigner(signer.getPath());
        certNode.setSignatureSha256(SignUtils.getSignature(certNode.contentToSign(), SignUtils.ALGORITHM_SHA256WithRSA, signer.getKeyPair().getPrivate()));
        certNode.setSignatureMd5(SignUtils.getSignature(certNode.contentToSign(), SignUtils.ALGORITHM_MD5WithRSA, signer.getKeyPair().getPrivate()));
        return NodeController.newNode(certNode);
    }
    */


}
