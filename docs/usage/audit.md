# @Audited Annotation  
>Use this annotation to mark the entities that need to be audited

## Maven Dependency
### Add this dependency in pom.xml
````xml
<dependency>
    <groupId>com.github.venkatramanm.swf-all</groupId>
    <artifactId>swf-plugin-audit</artifactId>
    <version>2.10</version>
</dependency>

````
### Imports required
#### Import this inside the .java file
* import com.venky.swf.plugins.audit.db.model.Audited

## Example
### Lets say we have a table Contact which we want to Audit.

````java
@Audited 
public interface Contact extends Model {

    public String getOwner();
    public void setOwner(String owner);
}
````
### Where does audit data go?
> - Any change to table annotated with **@Audited** will be stored in **ModelAudit** table in the database.  
> - ModelAudit stores the name of the Audited table in **NAME** column and ID of the edited row in **MODEL_ID** column
> - All the audited fields can be found in **COMMENT** column
> - **Sample Comment Value**  
>   - Lets say we changed **owner** field in **Contact** table. Below will be stored in **comment**
>   ````json
>    {
>        "owner": {
>            "old": "rohit",
>            "new": "aman"
>        }
>     }







