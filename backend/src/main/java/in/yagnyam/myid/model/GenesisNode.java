package in.yagnyam.myid.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Subclass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter
@Getter
@ToString
@Subclass(index=true)
@Cache
public class GenesisNode extends EntityNode {

    public static final String NODE_TYPE_VALUE = GenesisNode.class.getName();

    public static final String GENESIS_NODE_PATH = "/";

    @Override
    public String getNodeType() {
        return NODE_TYPE_VALUE;
    }

}
