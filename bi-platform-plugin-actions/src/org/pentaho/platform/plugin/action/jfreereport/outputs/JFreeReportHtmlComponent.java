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
 */
package org.pentaho.platform.plugin.action.jfreereport.outputs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.jfree.report.JFreeReport;
import org.jfree.report.JFreeReportBoot;
import org.jfree.report.ReportProcessingException;
import org.jfree.report.layout.output.YieldReportListener;
import org.jfree.report.modules.output.table.base.StreamReportProcessor;
import org.jfree.report.modules.output.table.html.AllItemsHtmlPrinter;
import org.jfree.report.modules.output.table.html.HtmlOutputProcessor;
import org.jfree.report.modules.output.table.html.HtmlPrinter;
import org.jfree.report.modules.output.table.html.StreamHtmlOutputProcessor;
import org.jfree.report.modules.output.table.html.URLRewriter;
import org.jfree.repository.ContentIOException;
import org.jfree.repository.ContentLocation;
import org.jfree.repository.DefaultNameGenerator;
import org.jfree.repository.NameGenerator;
import org.jfree.repository.file.FileRepository;
import org.jfree.repository.stream.StreamRepository;
import org.jfree.util.Configuration;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoURLRewriter;
import org.pentaho.platform.plugin.action.jfreereport.repository.ReportContentRepository;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportHtmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -4296469329232291213L;

  private static final boolean DO_NOT_USE_THE_CONTENT_REPOSITORY = true;

  public JFreeReportHtmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "text/html"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".html"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport(final JFreeReport report, final OutputStream outputStream) {
    try {
      IContentRepository contentRepository = null;
      try {
        contentRepository = PentahoSystem.getContentRepository(getSession());
      } catch (Throwable t) {
        debug(Messages.getString("JFreeReportHtmlComponent.DEBUG_0044_PROCESSING_WITHOUT_CONTENT_REPOS"), t); //$NON-NLS-1$
      }

      String contentHandlerPattern = getInputStringValue(AbstractJFreeReportComponent.REPORTHTML_CONTENTHANDLER);
      if (contentHandlerPattern == null) {
        final Configuration globalConfig = JFreeReportBoot.getInstance().getGlobalConfig();
        contentHandlerPattern = globalConfig.getConfigProperty("org.pentaho.web.ContentHandler"); //$NON-NLS-1$
      }

      final IApplicationContext ctx = PentahoSystem.getApplicationContext();

      final URLRewriter rewriter;
      final ContentLocation dataLocation;
      final NameGenerator dataNameGenerator;
      if ((contentRepository == null) || JFreeReportHtmlComponent.DO_NOT_USE_THE_CONTENT_REPOSITORY) {
        debug(Messages.getString("JFreeReportHtmlComponent.DEBUG_0044_PROCESSING_WITHOUT_CONTENT_REPOS")); //$NON-NLS-1$
        if (ctx != null) {
          File dataDirectory = new File(ctx.getFileOutputPath("system/tmp/"));//$NON-NLS-1$
          if (dataDirectory.exists() && (dataDirectory.isDirectory() == false)) {
            dataDirectory = dataDirectory.getParentFile();
            if (dataDirectory.isDirectory() == false) {
              throw new ReportProcessingException(Messages.getErrorString(
                  "JFreeReportDirectoryComponent.ERROR_0001_INVALID_DIR", dataDirectory.getPath())); //$NON-NLS-1$
            }
          } else if (dataDirectory.exists() == false) {
            dataDirectory.mkdirs();
          }

          final FileRepository dataRepository = new FileRepository(dataDirectory);
          dataLocation = dataRepository.getRoot();
          dataNameGenerator = new DefaultNameGenerator(dataLocation);
          rewriter = new PentahoURLRewriter(contentHandlerPattern);
        } else {
          dataLocation = null;
          dataNameGenerator = null;
          rewriter = new PentahoURLRewriter(contentHandlerPattern);
        }
      } else {
        debug(Messages.getString("JFreeReportHtmlComponent.DEBUG_045_PROCESSING_WITH_CONTENT_REPOS")); //$NON-NLS-1$
        final String thePath = getSolutionName() + "/" + getSolutionPath() + "/" + getSession().getId();//$NON-NLS-1$//$NON-NLS-2$
        final IContentLocation pentahoContentLocation = contentRepository.newContentLocation(thePath, getActionName(),
            getActionTitle(), getSolutionPath(), true);
        // todo
        final ReportContentRepository repository = new ReportContentRepository(pentahoContentLocation, getActionName());
        dataLocation = repository.getRoot();
        dataNameGenerator = new DefaultNameGenerator(dataLocation);
        rewriter = new PentahoURLRewriter(contentHandlerPattern);
      }

      final StreamRepository targetRepository = new StreamRepository(null, outputStream);
      final ContentLocation targetRoot = targetRepository.getRoot();

      final HtmlOutputProcessor outputProcessor = new StreamHtmlOutputProcessor(report.getConfiguration());
      final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
      printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "index", "html"));//$NON-NLS-1$//$NON-NLS-2$
      printer.setDataWriter(dataLocation, dataNameGenerator);
      printer.setUrlRewriter(rewriter);
      outputProcessor.setPrinter(printer);

      final StreamReportProcessor sp = new StreamReportProcessor(report, outputProcessor);
      final int yieldRate = getYieldRate();
      if (yieldRate > 0) {
        sp.addReportProgressListener(new YieldReportListener(yieldRate));
      }
      sp.processReport();
      sp.close();

      outputStream.flush();
      close();
      return true;
    } catch (ReportProcessingException e) {
      error(Messages.getString("JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    } catch (IOException e) {
      error(Messages.getString("JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    } catch (ContentIOException e) {
      error(Messages.getString("JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT"), e); //$NON-NLS-1$
      return false;
    }
  }
}
