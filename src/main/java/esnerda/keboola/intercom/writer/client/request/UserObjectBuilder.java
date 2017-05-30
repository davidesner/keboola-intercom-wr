/*
 */
package esnerda.keboola.intercom.writer.client.request;

import java.util.List;
import java.util.Map;

import esnerda.keboola.intercom.writer.client.IntercomValidationException;
import esnerda.keboola.intercom.writer.client.request.CompanyObjectBuilder.CompanyStaticColumns;
import io.intercom.api.Company;
import io.intercom.api.CustomAttribute;
import io.intercom.api.User;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class UserObjectBuilder {

    private final User user;

    public UserObjectBuilder(Map<String, String> userColValues, Map<UserStaticColumns, String> columnMapping) throws IntercomValidationException {
        

        this.user = new User();

        //set user static columns
        
            //set id
            String value;
            String key;
          
            key = getUserColumnKey(UserStaticColumns.user_id,columnMapping);
            if ((value = userColValues.get(key)) != null) {
                this.user.setUserId(value);
            } else {
                throw new IntercomValidationException("User ID column is not specified, you must specify it.", "User ID is not specified.",null);
            }

            //set email
           key = getUserColumnKey(UserStaticColumns.email,columnMapping);
            if ((value = userColValues.get(key)) != null) {
                this.user.setEmail(value);
            }

            //set signed_up_at
             key = getUserColumnKey(UserStaticColumns.signed_up_at,columnMapping);
               if ((value = userColValues.get(key)) != null) {
                try {
                    this.user.setSignedUpAt(Long.valueOf(value));
                } catch (NumberFormatException ex) {
                   throw new IntercomValidationException("Datatype of 'signed_up_at' attribute is invalid! Row ID: "+this.user.getUserId()+" not updated.", "Datatype of 'signed_up_at' attribute is invalid!",this.user.getUserId());
                }
            }
            //set name
             key = getUserColumnKey(UserStaticColumns.name,columnMapping);
                if ((value = userColValues.get(key)) != null) {
                this.user.setName(value);
            }
                
             //set last_seen_ip
             key = getUserColumnKey(UserStaticColumns.last_seen_ip,columnMapping);
                if ((value = userColValues.get(key)) != null) {
                this.user.setLastSeenIp(value);
            }    
             //set last_seen_user_agent
             key = getUserColumnKey(UserStaticColumns.last_seen_user_agent,columnMapping);
                if ((value = userColValues.get(key)) != null) {
                this.user.setUserAgentData(value);
            }    
             //set last_request_at
             key = getUserColumnKey(UserStaticColumns.last_request_at,columnMapping);
               if ((value = userColValues.get(key)) != null) {
                try {
                    this.user.setSignedUpAt(Long.valueOf(value));
                } catch (NumberFormatException ex) {
                    throw new IntercomValidationException("Datatype of 'last_request_at' attribute is invalid! Row ID: "+this.user.getUserId()+" not updated.", "Datatype of 'last_request_at' attribute is invalid!"+ 
                            "Row ID: "+this.user.getUserId()+" not updated. \n Long value expected, '"+value+"' found instead.",this.user.getUserId());
                
                }
            }  
             //set unsubscribed_from_emails
             key = getUserColumnKey(UserStaticColumns.unsubscribed_from_emails,columnMapping);
               if ((value = userColValues.get(key)) != null) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    this.user.setUnsubscribedFromEmails(Boolean.valueOf(value));
                }else{
                                        throw new IntercomValidationException("Datatype of 'unsubscribed_from_emails' attribute is invalid! Row ID: "+this.user.getUserId()+" not updated.", "Datatype of 'unsubscribed_from_emails' attribute is invalid!"+ 
                            "Row ID: "+this.user.getUserId()+" not updated. \n boolean value ('true','false') expected, '"+value+"' found instead.",this.user.getUserId());
                
                }
               }
             //set update_last_request_at
             key = getUserColumnKey(UserStaticColumns.update_last_request_at,columnMapping);
               if ((value = userColValues.get(key)) != null) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    this.user.setUpdateLastRequestAt(Boolean.valueOf(value));
                }else{
                                 throw new IntercomValidationException("Datatype of 'update_last_request_at' attribute is invalid! Row ID: "+this.user.getUserId()+" not updated.", "Datatype of 'update_last_request_at' attribute is invalid!"+ 
                            "Row ID: "+this.user.getUserId()+" not updated. \n boolean value ('true','false') expected, '"+value+"' found instead.",this.user.getUserId());
                
                }
            }  
             //set new_session
             key = getUserColumnKey(UserStaticColumns.new_session,columnMapping);
               if ((value = userColValues.get(key)) != null) {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    this.user.setNewSession(Boolean.valueOf(value));
                }else{
                      throw new IntercomValidationException("Datatype of 'new_session' attribute is invalid! Row ID: "+this.user.getUserId()+" not updated.", "Datatype of 'new_session' attribute is invalid!"+ 
                            "Row ID: "+this.user.getUserId()+" not updated. \n boolean value ('true','false') expected, '"+value+"' found instead.",this.user.getUserId());
                
                }
            }
               
               
               
    }
    
    public UserObjectBuilder setUserCustomColumns(Map<String, String> userColValues, List<CustomColumnMapping> columnMapping) throws IntercomValidationException{
     
         String value;
         if( columnMapping != null && !columnMapping.isEmpty()){
             for(CustomColumnMapping mapping : columnMapping){
                 CustomAttribute cust = null;
                  if (userColValues.containsKey(mapping.getSrcCol())) {
                      value = userColValues.get(mapping.getSrcCol());
                      String key = mapping.getDestCol();
                      
                      if(key==null ||key.isEmpty() || key.equals("")){// set key if value is not specified
                          key=mapping.getSrcCol();
                      }
                      /*create attribute*/
                    
                      
                       switch(mapping.getDataType()){
                          case String:
                               cust = CustomAttribute.newStringAttribute(key, value);
                               break;
                               
                          case Integer:
                             cust = CustomAttribute.newIntegerAttribute(key, Integer.valueOf(value)); 
                             break;
                             
                          case Double:
                              cust = CustomAttribute.newDoubleAttribute(key, Double.valueOf(value));
                              break;
                          case Float:
                              cust = CustomAttribute.newFloatAttribute(key, Float.valueOf(value));
                              break;
                              
                          case Long:
                              cust = CustomAttribute.newLongAttribute(key, Long.valueOf(value));
                              break;
                              
                          case Boolean:
                              if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    cust = CustomAttribute.newBooleanAttribute(key, Boolean.valueOf(value));
                }else{
                          throw new IntercomValidationException("Datatype of '"+key+"' attribute is invalid! Row ID: "+this.user.getUserId()+" not updated.", "Datatype of '"+key+"' attribute is invalid!"+ 
                            "Row ID: "+this.user.getUserId()+" not updated. \n boolean value ('true','false') expected, '"+value+"' found instead.",this.user.getUserId());
                
                }
                      break;                              
                      }
                     
                      if(cust==null) {
                        throw new IntercomValidationException("Invalid custom column type of column '"+key+"'!", "Invalid custom column type of column '"+key+"'!" ,this.user.getUserId());
                
                      }
                  }else{
                    throw new IntercomValidationException("Column '"+mapping.getSrcCol()+"' does not exist in source data!", "Column '"+mapping.getSrcCol()+"' does not exist in source data!" ,this.user.getUserId());
                
                  }
                      this.user.addCustomAttribute(cust);
            
             }
         }
         
         return this;
    }
    
    public UserObjectBuilder setCompany(Map<String, String> lineValues, Map<CompanyStaticColumns, String> columnMapping, List<CustomColumnMapping> customColumns){
        if(columnMapping!=null && !columnMapping.isEmpty()){
            Company c = new CompanyObjectBuilder(lineValues, columnMapping).setCustomColumns(lineValues, customColumns).build();
            if(c!=null){
        this.user.addCompany(new CompanyObjectBuilder(lineValues, columnMapping).setCustomColumns(lineValues, customColumns).build());        
            }
            }
        return this;
    }

public User build(){
    return this.user;
}

    private String getUserColumnKey(UserStaticColumns column, Map<UserStaticColumns, String> columnMapping) {
        boolean hasMapping = columnMapping != null && !columnMapping.isEmpty();
        String key;
        if (hasMapping) {
                key = columnMapping.get(column);
            if (key == null) {
                key = column.name();
            }
        } else {
            key = column.name();
        }

        return key;

    }

    public static enum UserStaticColumns {
        user_id, email, signed_up_at, name, last_seen_ip, last_seen_user_agent, last_request_at, unsubscribed_from_emails, update_last_request_at, new_session
    }


  
}
