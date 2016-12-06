package dbm.tw.DBmaestroAutomation;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.*;
import hudson.util.ArgumentListBuilder;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link TeamWorkFlowBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class TeamWorkFlowBuilder extends Builder {



    //From config.jelly
    private final String dbType;
    private final String commandType;
    private final String FlowName;
    private final String SourceEnvironmentName;
    private final String TargetEnvironmentName;
    private final String isBase;
    private final String NewLabelName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TeamWorkFlowBuilder(String dbType,String commandType,String FlowName,String SourceEnvironmentName,String TargetEnvironmentName
            ,String isBase,String NewLabelName) {
        this.dbType = dbType;
        this.commandType = commandType;
        this.FlowName = FlowName;
        this.SourceEnvironmentName = SourceEnvironmentName;
        this.TargetEnvironmentName = TargetEnvironmentName;
        this.isBase = isBase;
        this.NewLabelName = NewLabelName;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getDbType() {
        return dbType;
    }
    public String getcommandType() {
        return commandType;
    }
    public String getFlowName() {
        return FlowName;
    }
    public String getSourceEnvironmentName() {
        return SourceEnvironmentName;
    }
    public String getTargetEnvironmentName() {
        return TargetEnvironmentName;
    }
    public String getNewLabelName() {
        return NewLabelName;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try{
            String dbType = CommonFunctions.getDbTypeFromConfiguration(build);
            String twServer = CommonFunctions.getControllerHostFromConfiguration(build, dbType);
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
        }catch(Exception ex){
            listener.getLogger().println("unexpected exception has occurred: "+ex.toString());
            return false;
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    /**
     * Descriptor for {@link TeamWorkFlowBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/TeamWorkFlowBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        //From Global.jelly
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "DBmaestro TeamWork Plugin";
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return false;
        }


        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            req.bindJSON(this, formData);
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }
    }
}

