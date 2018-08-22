package donusgarcom.api;

import donusgarcom.api.common.ApplicationBinder;
import donusgarcom.api.resource.AuthResource;
import donusgarcom.api.resource.UserResource;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ext.ContextResolver;
import java.util.HashMap;
import java.util.Map;

public class ApiResourceConfig extends ResourceConfig {
    public ApiResourceConfig() {
        registerClasses(
                AuthResource.class,
                UserResource.class
        );
        register(new ApplicationBinder());
        register(getMoxyJsonResolver());
    }

    public ContextResolver<MoxyJsonConfig> getMoxyJsonResolver() {
        final MoxyJsonConfig moxyJsonConfig = new MoxyJsonConfig();
        Map<String, String> namespacePrefixMapper = new HashMap<>(1);
        namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        moxyJsonConfig.setNamespacePrefixMapper(namespacePrefixMapper).setNamespaceSeparator(':');
        return moxyJsonConfig.resolver();
    }
}
