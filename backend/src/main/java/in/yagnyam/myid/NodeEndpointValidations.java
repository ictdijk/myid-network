package in.yagnyam.myid;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.googlecode.objectify.Key;

import java.security.PublicKey;

import in.yagnyam.myid.model.EntityNode;
import in.yagnyam.myid.model.GenesisNode;
import in.yagnyam.myid.utils.PemUtils;
import in.yagnyam.myid.utils.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Slf4j
public class NodeEndpointValidations {

    private static void assertValidPublicKey(@NonNull String name, @NonNull String publicKey) throws BadRequestException {
        try {
            PemUtils.decodePublicKey(publicKey);
        } catch (Throwable t) {
            log.info("Invalid Public Key " + publicKey, t);
            throw new BadRequestException("Invalid " + name);
        }
    }

    private static void assertNotEmpty(@NonNull String name, String value) throws BadRequestException {
        assertTrue(!StringUtils.isEmpty(value), name + " is mandatory");
    }

    private static void assertNotNull(@NonNull String name, Object value) throws BadRequestException {
        assertTrue(value != null, name + " is mandatory");
    }

    public static void assertNoSuchNode(@NonNull String nodePath) throws ForbiddenException {
        // TODO: Risky, this should be part of transaction
        EntityNode existing = findNode(nodePath);
        if (existing != null) {
            log.info("{} already exists", existing);
            throw new ForbiddenException("Already exists, Forbidden");
        }
    }

    static void assertValidNode(EntityNode node) throws BadRequestException, ForbiddenException {
        assertNotNull("input", node);
        assertNotEmpty(EntityNode.PATH, node.getPath());
        assertNotEmpty(EntityNode.SIGNER, node.getSigner());
        assertNotEmpty(EntityNode.SIGNATURE_SHA256, node.getSignatureSha256());
        assertNotEmpty(EntityNode.SIGNATURE_MD5, node.getSignatureMd5());
        if (!StringUtils.isEmpty(node.getVerificationKey())) {
            assertValidPublicKey(EntityNode.VERIFICATION_KEY, node.getVerificationKey());
        }
        if (node.getPath().equals(GenesisNode.GENESIS_NODE_PATH)) {
            validateGenesisNode(node);
        } else {
            assertTrue(!node.getPath().equals(node.getSigner()), "Self signing is not allowed");
            validateNode(node);
        }
    }


    public static void validateGenesisNode(@NonNull EntityNode node) throws BadRequestException {
        assertTrue(node.getPath().equals(GenesisNode.GENESIS_NODE_PATH), "Node name must be " + GenesisNode.GENESIS_NODE_PATH + " for genesis node");
        assertTrue(node.getPath().equals(node.getSigner()), "Signer must be " + GenesisNode.GENESIS_NODE_PATH + " for genesis node");
        assertTrue(!StringUtils.isEmpty(node.getVerificationKey()), "Verification Key is mandatory");
        verifySignature(node, node.getVerificationKey());
    }

    private static void assertTrue(boolean value, String message) throws BadRequestException {
        if (!value) {
            log.info(message);
            throw new BadRequestException(message);
        }
    }

    private static EntityNode findNode(@NonNull String nodePath) throws ForbiddenException {
        return ofy().load().key(Key.create(EntityNode.class, nodePath)).now();
    }

    private static void validateNode(@NonNull EntityNode nodeToValidate) throws ForbiddenException, BadRequestException {
        EntityNode signer = (EntityNode)findNode(nodeToValidate.getSigner());
        if (signer == null) {
            log.info("No Signer available @ {}", nodeToValidate.getSigner());
            throw new BadRequestException("Invalid Signer " + nodeToValidate.getSigner());
        }
        if (StringUtils.isEmpty(signer.getVerificationKey())) {
            log.info(signer + " is not allowed to Sign");
            throw new BadRequestException("Invalid Signer " + signer);
        }
        verifySignature(nodeToValidate, signer.getVerificationKey());
    }

    private static void verifySignature(@NonNull EntityNode nodeToValidate, @NonNull String verificationKey) throws BadRequestException {
        log.debug("verifySignature({})", verificationKey);
        try {
            PublicKey publicKey = PemUtils.decodePublicKey(verificationKey);
            nodeToValidate.verifySignature(publicKey);
        } catch (Throwable t) {
            log.info("Unable to verify signature on " + nodeToValidate + " with key " + verificationKey, t);
            throw new BadRequestException("Unable to verify signature");
        }
    }

}
