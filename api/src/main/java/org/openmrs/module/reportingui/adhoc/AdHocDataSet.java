/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.reportingui.adhoc;

import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.column.definition.RowPerObjectColumnDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.RowPerObjectDataSetDefinition;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.Query;

import java.util.ArrayList;
import java.util.List;

public class AdHocDataSet {

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String uuid;

    @JsonProperty
    private String type;

    @JsonProperty
    private List<AdHocParameter> parameters;

    @JsonProperty
    private List<AdHocRowFilter> rowFilters;

    @JsonProperty
    private List<AdHocColumn> columns;

    public AdHocDataSet() {
    }

    public AdHocDataSet(RowPerObjectDataSetDefinition definition) {
        this.name = definition.getName();
        if (this.name.startsWith(AdHocExportManager.NAME_PREFIX)) {
            this.name = this.name.substring(AdHocExportManager.NAME_PREFIX.length());
        }
        this.description = definition.getDescription();
        this.uuid = definition.getUuid();
        this.type = definition.getClass().getName();
        for (Parameter parameter : definition.getParameters()) {
            addParameter(new AdHocParameter(parameter));
        }
        for (RowPerObjectColumnDefinition col : definition.getColumnDefinitions()) {
            addColumn(new AdHocColumn(col));
        }
        if (definition instanceof PatientDataSetDefinition) {
            PatientDataSetDefinition dsd = (PatientDataSetDefinition) definition;
            for (Mapped<? extends CohortDefinition> query : dsd.getRowFilters()) {
                addRowFilter(new AdHocRowFilter(query));
            }
        }
        else {
            throw new IllegalArgumentException("Not a handled type: " + definition.getClass().getName());
        }
    }

    public RowPerObjectDataSetDefinition toDataSetDefinition(AdHocExportManager adHocExportManager, AllDefinitionLibraries definitionLibraries) throws Exception {
        RowPerObjectDataSetDefinition dsd;

        if (uuid != null) {
            dsd = adHocExportManager.getAdHocDataSetByUuid(uuid);
            if (dsd == null) {
                throw new IllegalArgumentException("No data set definition with uuid " + uuid);
            }
            dsd.getParameters().clear();
            dsd.getColumnDefinitions().clear();
            if (dsd instanceof PatientDataSetDefinition) {
                ((PatientDataSetDefinition) dsd).getRowFilters().clear();
            }
        }
        else {
            dsd = (RowPerObjectDataSetDefinition) Context.loadClass(type).newInstance();
        }

        dsd.setName(name);
        dsd.setDescription(description);
        if (parameters != null) {
            for (AdHocParameter parameter : parameters) {
                dsd.addParameter(parameter.toParameter());
            }
        }
        if (columns != null) {
            for (AdHocColumn column : columns) {
                dsd.getColumnDefinitions().add(column.toColumnDefinition(definitionLibraries));
            }
        }
        if (rowFilters != null) {
            for (AdHocRowFilter filter : rowFilters) {
                Query query = filter.toQuery(dsd.getClass(), definitionLibraries);
                if (dsd instanceof PatientDataSetDefinition) {
                    ((PatientDataSetDefinition) dsd).addRowFilter(Mapped.mapStraightThrough((CohortDefinition) query));
                }
            }
        }
        return dsd;
    }

    public void addParameter(AdHocParameter adHocParameter) {
        if (parameters == null) {
            parameters = new ArrayList<AdHocParameter>();
        }
        parameters.add(adHocParameter);
    }

    public void addRowFilter(AdHocRowFilter adHocRowFilter) {
        if (rowFilters == null) {
            rowFilters = new ArrayList<AdHocRowFilter>();
        }
        rowFilters.add(adHocRowFilter);
    }

    public void addColumn(AdHocColumn adHocColumn) {
        if (columns == null) {
            columns = new ArrayList<AdHocColumn>();
        }
        columns.add(adHocColumn);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AdHocParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<AdHocParameter> parameters) {
        this.parameters = parameters;
    }

    public List<AdHocRowFilter> getRowFilters() {
        return rowFilters;
    }

    public void setRowFilters(List<AdHocRowFilter> rowFilters) {
        this.rowFilters = rowFilters;
    }

    public List<AdHocColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<AdHocColumn> columns) {
        this.columns = columns;
    }

}
