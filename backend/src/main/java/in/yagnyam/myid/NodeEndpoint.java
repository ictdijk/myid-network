/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package in.yagnyam.myid;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.googlecode.objectify.Key;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import in.yagnyam.myid.model.EntityNode;
import in.yagnyam.myid.utils.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static in.yagnyam.myid.NodeEndpointValidations.assertNoSuchNode;
import static in.yagnyam.myid.NodeEndpointValidations.assertValidNode;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "nodeApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "myid.yagnyam.in",
                ownerName = "myid.yagnyam.in",
                packagePath = ""
        )
)
@Slf4j
public class NodeEndpoint {

    static {
        StaticRegistrar.register();
    }

    static void populateNode(EntityNode node) {
    }

    private static String getNodePath(@NonNull String requestUrl) {
        String nodePath = requestUrl.replaceFirst("/nodes/", "");
        nodePath = nodePath.replaceFirst("^/+", "");
        nodePath = nodePath.replaceFirst("/+$", "");
        nodePath = "/" + nodePath;
        return nodePath;
    }

    @ApiMethod(name = "postNode", path = "nodes", httpMethod = ApiMethod.HttpMethod.POST)
    public EntityNode postNode(HttpServletRequest request, BlockChainNode node) throws BadRequestException, ForbiddenException {
        log.info("postNode {}", node);
        return newNode(NodeFactory.fromJson(node));
    }


    static EntityNode newNode(final EntityNode node) throws BadRequestException, ForbiddenException {
        // Do basic validations
        assertValidNode(node);
        // TODO: Risky, this should be part of transaction
        assertNoSuchNode(node.getPath());
        // Enrich if needed
        populateNode(node);
        // Add to DB
        ofy().transact(new Runnable() {
            @Override
            public void run() {
                // assertNoSuchNode(node.getPath());
                ofy().save().entity(node);
            }
        });
        return node;
    }


    @ApiMethod(name = "fetchNode", path = "node", httpMethod = ApiMethod.HttpMethod.GET)
    public EntityNode fetchNode(HttpServletRequest request, @Named("path") String nodePath) {
        log.info("fetchNode {}", nodePath);
        EntityNode result = ofy().load().key(Key.create(EntityNode.class, nodePath)).now();
        log.info("fetchNode({}) => {}", nodePath, result);
        return result;
    }


    @ApiMethod(name = "allNodes", path = "nodes", httpMethod = ApiMethod.HttpMethod.GET)
    public List<EntityNode> allNodes() {
        log.info("allNodes");
        List<EntityNode> results = ofy().load().type(EntityNode.class).order(EntityNode.CREATION_TIME).list();
        log.info("allNodes {} Done", results);
        return results;
    }

}
