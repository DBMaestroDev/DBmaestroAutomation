# DBmaestroAutomation
Provides an easy to use UI extension for DBmaestro TeamWork Automation feature in the familiar Jenkins DevOp. This plugin exposes to the user additional DBmaestro build steps in Free style projects to connect to TeamWork automation command line tool: Generate, Build, Deploy, Checkin, Apply Labels and Set baselines, all with an easy on time or job by job configuration. Supports both SQL and Oracle TeamWork enterprise Solution versions Since version 5.1.1.

Full list of added build steps:
Add objects to source control - Adds objects to source control, without using optional parameters defaults to add all objects in environment.
DBmaestro Apply Label - Creates label on specified environment's schemas.
DBmaestro Apply label and set baseline - Creates new label and sets as baseline in from environment as to the baseline environment.
DBmaestro Check in - Check-in objects to source control, without using optional parameters defaults to check-in all managed objects in environment.
DBmaestro Check out - Checks out all objects in an environment.
DBmaestro Deploy - Executes all scripts in Source environment planned folder on Target environment. Creates Label on target after successful deploy and sets as baseline on source.
DBmaestro Enter Integration state - Enters all Users/Schema in an environment into Integration state.
DBmaestro Exit Integration state - Exits all Users/Schema in an environment from Integration state.
DBmaestro Generate\Build latest revision - Applies label and generates deployment script from a predefined Pipeline project & environment to its mapped target environment using source environments latest revision. (Build will delete existing scripts)
DBmaestro Generate\Build specific label -Generates deployment script from a predefined Pipeline project & environment to its mapped target environment using a source environments specific label name. (Build will delete existing scripts)
DBmaestro Generate\Build between labels - Generates deployment script from a predefined Pipeline project & environment to its mapped target environment, between a set of existing label names. (Build will delete existing scripts)
DBmaestro Generate \Build by tasks - Generates deployment script from a predefined Pipeline project & environment to its mapped target environment using a source environments list of tasks. (Build will delete existing scripts)
DBmaestro Set baseline - Sets an existing label as baseline from an environment to baseline environment.
DBmaestro Validate state - Runs comparison without generating scripts to validate if environments are identical. Results in failure code if not identical.

More about DBmaestro & TeamWork:
DBmaestro is a pioneer and a leading DevOps for database solution provider. Its flagship product, DBmaestro TeamWork, enables Agile development and Continuous Integration and Delivery for the Database.
TeamWork supports streamlining of development process management and enforcing change policy practices.
The solution empowers agile team collaboration while fostering regulatory compliance and governance.
With DBmaestro, organizations can facilitate DevOps for database by executing deployment automation, enhancing and reinforcing security as well as mitigating risk.
"# DBmaestroAutomation" 
