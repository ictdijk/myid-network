package in.yagnyam.myid.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RootNodeSignature {

    private String signer;

    private String signature;
}
