package dbm.tw.DBmaestroAutomation;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ArgumentListBuilder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


public class TeamWorkConfiguration extends Builder {


    private final String dbType;
    private final String controllerHost;
    
    @SuppressWarnings("unused")
    public String ControllerHost() {
        return controllerHost;
    }

    @SuppressWarnings("unused")
    public String DbType() {
        return dbType;
    }


    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TeamWorkConfiguration(String DbType, String ControllerHost
                                 //,boolean UseAuthorization, String WindowsUsername, String WindowsPassword
    ) {
        this.dbType = DbType;
        this.controllerHost = ControllerHost;        
    }


    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            ArgumentListBuilder args = new ArgumentListBuilder();

            if (!launcher.isUnix()) {
                args.add("cmd.exe");
                args.add("/C");
            }
            args.add("java");
            args.add("-jar");
            args = CommonFunctions.AddQuoted(args, CommonFunctions.getJarPathFromConfiguration(build));
            args.add("-help");

            listener.getLogger().println("Prepared command line, " + args.toString());
            String result = CommonFunctions.DoRuntime(build, launcher, listener, args);
            listener.getLogger().println("result from command line: " + result);
        } catch (Exception ex) {
            listener.getLogger().println("unexpected exception has occurred: " + ex.toString());
            return false;
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public pluginDescriptorImpl getDescriptor() {
        return (pluginDescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link TeamWorkConfiguration}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class pluginDescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         * <p/>
         * <p/>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        //From Global.jelly
        private String clientRunPath;
        private String sqlControllerHost;
        private String oracleControllerHost;
        private String defaultDbType;
        //        private boolean authenticationRequired;
        private boolean useAdvancedMode;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public pluginDescriptorImpl() {
            load();
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "DBmaestro Configuration";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return false;
        }

        public String getClientRunPath() {
            return clientRunPath;
        }

        public String getSqlControllerHost() {
            return sqlControllerHost;
        }

        public String getOracleControllerHost() {
            return oracleControllerHost;
        }

        public String getDefaultDbType() {
            return defaultDbType;
        }


        public boolean getUseAdvancedMode() {
            return useAdvancedMode;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            req.bindJSON(this, formData);
            clientRunPath = formData.getString("clientRunPath");
            sqlControllerHost = formData.getString("sqlControllerHost");
            oracleControllerHost = formData.getString("oracleControllerHost");
            defaultDbType = formData.getString("defaultDbType");
            useAdvancedMode = formData.getBoolean("useAdvancedMode");
            
            save();
            return super.configure(req, formData);
        }
    }
}

