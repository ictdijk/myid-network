package in.yagnyam.myid.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import in.yagnyam.myid.BlockChainNode;
import in.yagnyam.myid.utils.SignUtils;
import in.yagnyam.myid.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter
@Getter
@ToString
@Slf4j
@Entity
public class EntityNode {

    public static final String KIND = EntityNode.class.getSimpleName();
    public static final String NODE_TYPE_VALUE = EntityNode.class.getName();

    public static final String NODE_TYPE = "nodeType";
    public static final String PATH = "path";
    public static final String CREATION_TIME = "creationTime";
    // Content
    public static final String DESCRIPTION = "description";
    public static final String DATA_HASH_SHA256 = "dataHashSha256";
    public static final String DATA_HASH_MD5 = "dataHashMd5";
    public static final String VERIFICATION_KEY = "verificationKey";
    // TODO: Only for development
    public static final String PRIVATE_KEY = "privateKey";

    public static final String SIGNER = "signer";
    public static final String SIGNATURE_SHA256 = "signatureSha256";
    public static final String SIGNATURE_MD5 = "signatureMd5";

    @Id
    private String path;

    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long creationTime = System.currentTimeMillis();

    private String description;

    private String dataHashSha256;

    private String dataHashMd5;

    // TODO: Only for development
    private String privateKey;

    private String verificationKey;

    @Index
    private String signer;

    private String signatureSha256;

    private String signatureMd5;

    @Ignore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private KeyPair keyPair;

    public String getNodeType() {
        return NODE_TYPE_VALUE;
    }

    public void setNodeType(String nodeType) {
    }

    public void populateFrom(@NonNull BlockChainNode blockChainNode) {
        setPath(blockChainNode.getPath());
        setDescription(blockChainNode.getDescription());
        setDataHashSha256(blockChainNode.getDataHashSha256());
        setDataHashMd5(blockChainNode.getDataHashMd5());
        setPrivateKey(urlDecode(blockChainNode.getPrivateKey()));
        setVerificationKey(urlDecode(blockChainNode.getVerificationKey()));
        log.debug("Verification Key: {}", getVerificationKey());
        setSigner(blockChainNode.getSigner());
        setSignatureSha256(blockChainNode.getSignatureSha256());
        setSignatureMd5(blockChainNode.getSignatureMd5());
    }


    private static String urlDecode(String value) {
        return value == null ? null : URLDecoder.decode(value);
    }

    public boolean verifySignature(PublicKey verificationKey) throws GeneralSecurityException, IOException {
        String content = contentToSign();
        log.debug("verifySignature Sha256: {}, Md5: {} on {}", signatureSha256, signatureMd5, content);
        // Include path in data

        return SignUtils.verifySignature(content, SignUtils.ALGORITHM_SHA256WithRSA, verificationKey, signatureSha256)
                && SignUtils.verifySignature(content, SignUtils.ALGORITHM_MD5WithRSA, verificationKey, signatureMd5);
    }

    public String contentToSign() {
        return getPath()
                + StringUtils.nonNull(getDescription())
                + StringUtils.nonNull(getDataHashSha256())
                + StringUtils.nonNull(getDataHashMd5())
                + StringUtils.nonNull(getVerificationKey());
    }

}
