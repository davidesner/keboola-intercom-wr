/*
 */
package esnerda.keboola.intercom.writer.client.request;

import java.util.List;
import java.util.Map;

import io.intercom.api.Company;
import io.intercom.api.CustomAttribute;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class CompanyObjectBuilder {

    private final Company company;

    public CompanyObjectBuilder(Map<String, String> colValues, Map<CompanyStaticColumns, String> columnMapping) {
        

        this.company = new Company() ;

        //set user static columns
        
            //set id
            String value;
            String key;
          
            key = getCompanyColumnKey(CompanyStaticColumns.company_id,columnMapping);
            if ((value = colValues.get(key)) != null) {
                this.company.setCompanyID(value);
            } else {
                //TODO: throw exception
            }

            //set plan
           key = getCompanyColumnKey(CompanyStaticColumns.company_plan,columnMapping);
            if ((value = colValues.get(key)) != null) {
                this.company.setPlan(new Company.Plan(value));
            }

            //set monthly_spend
             key = getCompanyColumnKey(CompanyStaticColumns.company_monthly_spend,columnMapping);
               if ((value = colValues.get(key)) != null) {
                try {
                    this.company.setMonthlySpend(Float.valueOf(value));
                } catch (NumberFormatException ex) {
                   //TODO: throw exception
                }
            }
            //set name
             key = getCompanyColumnKey(CompanyStaticColumns.company_name,columnMapping);
                if ((value = colValues.get(key)) != null) {
                this.company.setName(value);
            }
                
 
             //set remote_created_at
             key = getCompanyColumnKey(CompanyStaticColumns.company_remote_created_at,columnMapping);
               if ((value = colValues.get(key)) != null) {
                try {
                    this.company.setRemoteCreatedAt(Long.valueOf(value));
                } catch (NumberFormatException ex) {
                    //TODO: throw exception
                }
            } 
            
            
               
               
               
    }
    
    public CompanyObjectBuilder setCustomColumns(Map<String, String> colValues, List<CustomColumnMapping> columnMapping){
     
         String value;
         if( columnMapping != null){
             for(CustomColumnMapping mapping : columnMapping){
                 CustomAttribute cust = null;
                  if ((value = colValues.get(mapping.getSrcCol())) != null) {
                      String key = mapping.getDestCol();
                      
                      if(key==null ||key.isEmpty() || key.equals("")){// set key if value is not specified
                          key=mapping.getSrcCol();
                      }
                      /*create attribute*/
                      //TODO: handle different types
                      
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
                    //TODO: throw exception
                    //throw exception
                }
                              break;
                              
                      }
                     
                      if(cust==null) {
//TODO: throw exception
                      }
                  }else{
                   //TODO: throw exception on missing column name in data   
                  }
                      this.company.addCustomAttribute(cust);
            
             }
         }
         
         return this;
    }
    
    public Company build(){
        //return null on empty company
        if(this.company.getCompanyID()==null) return null;
        return this.company;
    }

    private String getCompanyColumnKey(CompanyStaticColumns column, Map<CompanyStaticColumns, String> columnMapping) {
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

    public static enum CompanyStaticColumns {
        company_remote_created_at, company_id, company_name, company_monthly_spend, company_plan
    }

    public class UserValidationException extends Exception {

        private String shortMessage;

        UserValidationException(String message, String detailedMessage) {
            super(message);
            this.shortMessage = detailedMessage;

        }

        public String getShortMessage() {
            return shortMessage;
        }

    }
}
