package in.yagnyam.myid;

import org.jose4j.base64url.Base64;

import in.yagnyam.myid.utils.AsnSerializer;
import in.yagnyam.myid.utils.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BlockChainNode {

    private String path;

    private String description;

    private String dataHashSha256;

    private String dataHashMd5;

    // TODO: Only for Development
    private String privateKey;

    private String verificationKey;

    private String signer;

    private String signatureSha256;

    private String signatureMd5;

    @SneakyThrows
    public String contentToSign() {
        AsnSerializer serializer = new AsnSerializer();
        serializer.addString(description);
        serializer.addString(dataHashSha256);
        serializer.addString(dataHashMd5);
        serializer.addString(verificationKey);
        return Base64.encode(serializer.getEncoded());
        /*
        return path
                + StringUtils.nonNull(description)
                + StringUtils.nonNull(dataHashSha256)
                + StringUtils.nonNull(dataHashMd5)
                + StringUtils.nonNull(verificationKey);
                */
    }


}
