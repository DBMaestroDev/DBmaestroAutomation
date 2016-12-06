package dbm.tw.DBmaestroAutomation;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.AbstractIdCredentialsListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collections;

/**
 * Created by yonathand on 11/23/2015.
 */
public class TeamWorkJobProperty extends JobProperty<AbstractProject<?,?>>  {
    //From Config.jelly
    private final String controllerHost;
    private final String dbType;
    private final String jarPath;
    private final boolean useAuthorization;
    private final String windowsLdapServer;
    private final String windowsDomain;
    private final String windowsUsername;
    private final Secret windowsPassword;
    private final String credentialsId;

    public String getControllerHost()
    {
        return controllerHost;
    }
    public String getJarPath()
    {
        return jarPath;
    }
    public String getDbType()
    {
        return dbType;
    }
    public boolean getUseAuthorization()
    {
        return useAuthorization;
    }
    public String getWindowsLdapServer()
    {
        return windowsLdapServer;
    }
    public String getWindowsDomain()
    {
        return windowsDomain;
    }
    public String getWindowsUsername()
    {
        return windowsUsername;
    }
    public String getWindowsPassword()
    {
        return windowsPassword.getPlainText();
    }

    public final String getCredentialsId()
    {
        return credentialsId;
    }

    @DataBoundConstructor
    public TeamWorkJobProperty(String dbType,
                               String controllerHost,
                               String jarPath,
                               boolean useAuthorization,
                               String windowsLdapServer,
                               String windowsDomain,
                               String windowsUsername,
                               String windowsPassword,
                               String credentialsId) {
        this.dbType = dbType;
        this.controllerHost = controllerHost;
        this.jarPath = jarPath;
        this.useAuthorization = useAuthorization;
        this.windowsLdapServer = windowsLdapServer;
        this.windowsDomain = windowsDomain;
        this.windowsUsername = windowsUsername;
        this.windowsPassword = Secret.fromString(windowsPassword);
        this.credentialsId = credentialsId;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "DBmaestro TeamWork Properties";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req,formData);
        }
        // fills credential list.
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item owner) {
            if (owner == null || !owner.hasPermission(Item.CONFIGURE)) {
                return new ListBoxModel();
            }
            // when configuring the job, you only want those credentials that are available to ACL.SYSTEM selectable
            // as we cannot select from a user's credentials unless they are the only user submitting the build
            // (which we cannot assume) thus ACL.SYSTEM is correct here.


            return new Model().withEmptySelection().withAll(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, owner, ACL.SYSTEM, Collections.<DomainRequirement>emptyList()));
        }
        //gets credential display name.
        private final class Model extends AbstractIdCredentialsListBoxModel<Model, StandardUsernamePasswordCredentials> {

            @Override protected String describe(StandardUsernamePasswordCredentials c) {
                return CredentialsNameProvider.name(c);
            }

        }
    }
}
