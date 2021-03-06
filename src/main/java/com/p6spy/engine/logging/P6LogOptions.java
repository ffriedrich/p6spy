/*
 * #%L
 * P6Spy
 * %%
 * Copyright (C) 2013 P6Spy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.p6spy.engine.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.StandardMBean;

import com.p6spy.engine.leak.P6LeakOptionsMBean;
import com.p6spy.engine.spy.P6ModuleManager;
import com.p6spy.engine.spy.option.P6OptionsRepository;

public class P6LogOptions extends StandardMBean implements P6LogLoadableOptions {

  public static final String EXCLUDE = "exclude";
  public static final String INCLUDE = "include";
  public static final String FILTER = "filter";
  public static final String EXCLUDECATEGORIES = "excludecategories";
  public static final String EXECUTION_THRESHOLD = "executionThreshold";
  public static final String SQLEXPRESSION = "sqlexpression";

  // those set indirectly (via properties visible from outside) 
  public static final String INCLUDE_TABLES = "includeTables";
  public static final String EXCLUDE_TABLES = "excludeTables";
  public static final String INCLUDE_TABLES_PATTERN = "includeTablesPattern";
  public static final String EXCLUDE_TABLES_PATTERN = "excludeTablesPattern";
  public static final String EXCLUDECATEGORIES_SET = "excludecategoriesSet";
  public static final String SQLEXPRESSION_PATTERN = "sqlexpressionPattern";
  
  public static final Map<String, String> defaults;
  
  static {
    defaults = new HashMap<String, String>();
    
    defaults.put(FILTER, Boolean.toString(false));
    defaults.put(EXCLUDECATEGORIES, "info,debug,result,resultset,batch");
    defaults.put(EXECUTION_THRESHOLD, Long.toString(0));
  }

  private final P6OptionsRepository optionsRepository;

  public P6LogOptions(final P6OptionsRepository optionsRepository) {
    super(P6LogOptionsMBean.class, false);
    this.optionsRepository = optionsRepository;
  }
  
  @Override
  public void load(Map<String, String> options) {
    
    setSQLExpression(options.get(SQLEXPRESSION));
    setExecutionThreshold(options.get(EXECUTION_THRESHOLD));
    
    setExcludecategories(options.get(EXCLUDECATEGORIES));
    
    setFilter(options.get(FILTER));
    setInclude(options.get(INCLUDE));
    setExclude(options.get(EXCLUDE));
  }

  /**
   * Utility method, to make accessing options from app less verbose.
   * 
   * @return active instance of the {@link P6LogLoadableOptions}
   */
  public static P6LogLoadableOptions getActiveInstance() {
    return P6ModuleManager.getInstance().getOptions(P6LogOptions.class);
  }

  @Override
  public Map<String, String> getDefaults() {
    return defaults;
  }

  // JMX exposed API
  
  @Override
  public void setExclude(String exclude) {
    optionsRepository.set(String.class, EXCLUDE, exclude);
    optionsRepository.setSet(String.class, EXCLUDE_TABLES, exclude);
    
    final Set<String> tables = optionsRepository.getSet(String.class, EXCLUDE_TABLES);
    // please note: in case of add+remove from the tables set it might become empty
    // then pattern would be null => won't replace the old value, but in business logic
    // we check for tables.isEmpty() => won't go for pattern in that case, see: P6LogQuery.isQueryOk()
    optionsRepository.set(Pattern.class, EXCLUDE_TABLES_PATTERN, getPattern(tables));
  }

  /**
   * @param tableNames
   *          table names.
   * @return regexp string matching table names in {@code SQL} statement {@code FROM} clause.
   */
  private String getPattern(final Set<String> tableNames) {
    if (null == tableNames || tableNames.isEmpty()) {
      return null;
    }
    
    final StringBuilder sb = new StringBuilder("select.*from(.*(");

    boolean isFirstOne = true;
    for (String tableName : tableNames) {
      if (!isFirstOne) {
        sb.append("|");
      } else {
        isFirstOne = false;
      }
      sb.append("(").append(tableName).append(")");
    }

    return sb.append(").*)(where|;|$)").toString();
  }

  @Override
  public String getExclude() {
    return optionsRepository.get(String.class, EXCLUDE);
  }

  @Override
  public void setExcludecategories(String excludecategories) {
    optionsRepository.set(String.class, EXCLUDECATEGORIES, excludecategories);
    optionsRepository.setSet(String.class, EXCLUDECATEGORIES_SET, excludecategories);
  }

  @Override
  public String getExcludecategories() {
    return optionsRepository.get(String.class, EXCLUDECATEGORIES);
  }

  @Override
  public void setFilter(String filter) {
    optionsRepository.set(Boolean.class, FILTER, filter);
  }
  
  @Override
  public void setFilter(boolean filter) {
    optionsRepository.set(Boolean.class, FILTER, filter);
  }

  @Override
  public boolean getFilter() {
    return optionsRepository.get(Boolean.class, FILTER);
  }

  @Override
  public void setInclude(String include) {
    optionsRepository.set(String.class, INCLUDE, include);
    optionsRepository.setSet(String.class, INCLUDE_TABLES, include);
    
    final Set<String> tables = optionsRepository.getSet(String.class, INCLUDE_TABLES);
    // please note: in case of add+remove from the tables set it might become empty
    // then pattern would be null => won't replace the old value, but in business logic
    // we check for tables.isEmpty() => won't go for pattern in that case, see: P6LogQuery.isQueryOk()
    optionsRepository.set(Pattern.class, INCLUDE_TABLES_PATTERN, getPattern(tables));
  }

  @Override
  public String getInclude() {
    return optionsRepository.get(String.class, INCLUDE);
  }

  @Override
  public String getSQLExpression() {
    return optionsRepository.get(String.class, SQLEXPRESSION);
  }
  
  @Override
  public Pattern getSQLExpressionPattern() {
    return optionsRepository.get(Pattern.class, SQLEXPRESSION_PATTERN);
  }

  @Override
  public void setSQLExpression(String sqlexpression) {
    optionsRepository.set(String.class, SQLEXPRESSION, sqlexpression);
    optionsRepository.set(Pattern.class, SQLEXPRESSION_PATTERN, sqlexpression);
  }

  @Override
  public void setExecutionThreshold(String executionThreshold) {
    optionsRepository.set(Long.class, EXECUTION_THRESHOLD, executionThreshold);
  }
  
  @Override
  public void setExecutionThreshold(long executionThreshold) {
    optionsRepository.set(Long.class, EXECUTION_THRESHOLD, executionThreshold);
  }

  @Override
  public long getExecutionThreshold() {
    return optionsRepository.get(Long.class, EXECUTION_THRESHOLD);
  }

  @Override
  public Set<String> getIncludeTables() {
    return optionsRepository.getSet(String.class, INCLUDE_TABLES);
  }

  @Override
  public Set<String> getExcludeTables() {
    return optionsRepository.getSet(String.class, EXCLUDE_TABLES);
  }
  
  @Override
  public Pattern getIncludeTablesPattern() {
    return optionsRepository.get(Pattern.class, INCLUDE_TABLES_PATTERN);
  }

  @Override
  public Pattern getExcludeTablesPattern() {
    return optionsRepository.get(Pattern.class, EXCLUDE_TABLES_PATTERN);
  }

  @Override
  public Set<String> getExcludeCategoriesSet() {
    return optionsRepository.getSet(String.class, EXCLUDECATEGORIES_SET);
  }
}
