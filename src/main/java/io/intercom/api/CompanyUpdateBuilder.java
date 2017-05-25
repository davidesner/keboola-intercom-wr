package io.intercom.api;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

class CompanyUpdateBuilder {

    /**
     * Provide restrictions on the company data that can be sent via a user update
     */
    static List<CompanyWithStringPlan> buildUserUpdateCompanies(CompanyCollection add, CompanyCollection remove) {

        final ArrayList<CompanyWithStringPlan> updatableCompanies = Lists.newArrayList();
        if (add != null) {
            final List<Company> companies = add.getPage();
            for (Company company : companies) {
                updatableCompanies.add(prepareUpdatableCompany(company));
            }
        }

        if (remove != null) {
            final List<Company> companies = remove.getPage();
            for (Company company : companies) {
                updatableCompanies.add(prepareUpdatableCompany(company).setRemove(Boolean.TRUE));
            }
        }

        return updatableCompanies;
    }

    private static CompanyWithStringPlan prepareUpdatableCompany(Company company) {
    	 final CompanyWithStringPlan updatableCompany = new CompanyWithStringPlan();
         updatableCompany.setId(company.getId());
         updatableCompany.setCompanyID(company.getCompanyID());
         updatableCompany.setName(company.getName());
         updatableCompany.setSessionCount(company.getSessionCount());
         updatableCompany.setMonthlySpend(company.getMonthlySpend());
         updatableCompany.setRemoteCreatedAt(company.getRemoteCreatedAt());
         if (company.getCustomAttributes() != null) {
             updatableCompany.getCustomAttributes().putAll(company.getCustomAttributes());
         }
         if (company.getPlan() != null) {
             updatableCompany.setPlan(company.getPlan().getName());
         }
         return updatableCompany;
     }
}
