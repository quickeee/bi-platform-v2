/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 07, 2006
 * @author Thomas Morgner
 */
package org.pentaho.platform.plugin.action.jfreereport.outputs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.io.IOUtils;
import org.jfree.report.JFreeReport;
import org.jfree.report.ResourceBundleFactory;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceBundleFactory;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * The base class for all content generating components. This class adds the report-data to the JFreeReport object. If
 * requested, it creates a private copy of the report before doing any work - cloning is faster than parsing.
 * <p/>
 * Sub-Actions of this component are usually the last step in the report processing.
 *
 * @author Thomas Morgner
 */
public abstract class AbstractGenerateContentComponent extends AbstractJFreeReportComponent {
  protected AbstractGenerateContentComponent() {
  }

  @Override
  protected boolean validateAction() {
    if (!(isDefinedInput(AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT))) {
      warn(Messages.getString("AbstractGenerateContentComponent.JFreeReport.ERROR_0038_NO_REPORT_OBJECT_INPUT")); //$NON-NLS-1$
      return false;
    }

    if (isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATE_YIELDRATE)) {
      final Object inputValue = getInputValue(AbstractJFreeReportComponent.REPORTGENERATE_YIELDRATE);
      if (inputValue instanceof Number) {
        final Number n = (Number) inputValue;
        if (n.intValue() < 0) {
          warn(Messages.getString("AbstractGenerateContentComponent.JFreeReport.ERROR_0040_YIELD_RATE_POSITIVE")); //$NON-NLS-1$
        }
      } else {
        warn(Messages.getString("AbstractGenerateContentComponent.JFreeReport.ERROR_0041_YIELD_RATE_NUMERIC")); //$NON-NLS-1$
        return false;
      }
    }
    if (isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYINPUT)) {
      final String inputValue = getInputStringValue(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYINPUT);
      if ( ( !(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYNORMAL.equals(inputValue)) )
          && ( !(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYLOWER.equals(inputValue)) )
          && ( !(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYLOWEST.equals(inputValue)) )) {
        warn(Messages.getString("AbstractGenerateContentComponent.JFreeReport.ERROR_0042_PRIORITY_MUST_BE")); //$NON-NLS-1$
      }
    }
    return true;
  }

  protected JFreeReport getReport() {
    final Object maybeJFreeReport = getInputValue(AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT);
    if (maybeJFreeReport instanceof JFreeReport) {
      return (JFreeReport) maybeJFreeReport;
    }
    return null;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    JFreeReport report = getReport();
    if (report == null) {
      warn(Messages.getString("AbstractGenerateContentComponent.JFreeReport.ERROR_0043_NO_REPORT_FOR_ACTION")); //$NON-NLS-1$
      return false;
    }

    applyThreadPriority();

    final boolean privateCopy = getInputBooleanValue(
        AbstractJFreeReportComponent.REPORTPARAMCOMPONENT_PRIVATEREPORT_OUTPUT, false);
    if (privateCopy && isDefinedOutput(AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT)) {
      report = (JFreeReport) report.clone();
    }

    // this might be invalid in case the action is contained in a sub-directory.
    final String baseName = IOUtils.getInstance().stripFileExtension(getActionName());
    final String path = getSolutionName() + File.separator + getSolutionPath();
    final PentahoResourceBundleFactory bundleFactory = new PentahoResourceBundleFactory(path, baseName, getSession());
    report.setResourceBundleFactory(bundleFactory);
    // set the default resourcebundle. This allows users to override the
    // resource-bundle in case they want to keep common strings in a common
    // collection.
    report.getReportConfiguration().setConfigProperty(ResourceBundleFactory.DEFAULT_RESOURCE_BUNDLE_CONFIG_KEY,
        baseName);

    return performExport(report);
  }

  private void applyThreadPriority() {
    if (isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYINPUT)) {
      try {
        final String inputValue = getInputStringValue(AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYINPUT);
        if (AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYLOWER.equals(inputValue)) {
          final int priority = Math.max(Thread.currentThread().getPriority() - 1, 1);
          Thread.currentThread().setPriority(priority);
        } else if (AbstractJFreeReportComponent.REPORTGENERATE_PRIORITYLOWEST.equals(inputValue)) {
          Thread.currentThread().setPriority(1);
        }
      } catch (Exception e) {
        // Non fatal exception.
        warn(Messages
            .getString("AbstractGenerateContentComponent.JFreeReport.ERROR_0044_UNABLE_T0_SET_THREAD_PRIORITY")); //$NON-NLS-1$
      }
    }
  }

  protected abstract boolean performExport(final JFreeReport report) throws IOException;

  @Override
  public void done() {

  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(getClass());
  }

  protected int getYieldRate() {
    if (isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATE_YIELDRATE)) {
      final Object inputValue = getInputValue(AbstractJFreeReportComponent.REPORTGENERATE_YIELDRATE);
      if (inputValue instanceof Number) {
        Number n = (Number) inputValue;
        if (n.intValue() < 1) {
          return 0;
        }
        return n.intValue();
      }
    }
    return 0;
  }

}
