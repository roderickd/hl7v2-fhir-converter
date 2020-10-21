/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.linuxforhealth.hl7.resource;

import java.io.File;
import java.io.IOException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import io.github.linuxforhealth.api.ResourceModel;
import io.github.linuxforhealth.core.Constants;
import io.github.linuxforhealth.core.ObjectMapperUtil;

/**
 * Reads resources. If the configuration file has base path defined (base.path.resource) then the
 * resources are loaded from that path. If the configuration is not defined then default path would
 * be used.
 * 
 *
 * @author pbhallam
 */
public class ResourceModelReader {

  private static ResourceModelReader reader;

  private File resourceFolder;

  private ResourceModelReader() {
    try {
    Configurations configs = new Configurations();
    // Read data from this file
    File propertiesFile = new File("config.properties");
      PropertiesConfiguration config;

      config = configs.properties(propertiesFile);

    String resourceLoc = config.getString("base.path.resource");
    if (StringUtils.isNotBlank(resourceLoc)) {
      resourceFolder = new File(resourceLoc);
    } else {
      resourceFolder = Constants.DEFAULT_HL7_RESOURCES;
    }
    } catch (ConfigurationException e) {
      throw new IllegalStateException("Cannot read configuration for resource location", e);
    }
  }


  public ResourceModel generateResourceModel(String path) {

    File templateFile = new File(resourceFolder, path + ".yml");

    if (templateFile.exists()) {
      try {
        HL7DataBasedResourceModel rm =
            ObjectMapperUtil.getYAMLInstance().readValue(templateFile,
                HL7DataBasedResourceModel.class);
        if (StringUtils.isBlank(rm.getName())
            || StringUtils.equalsIgnoreCase(rm.getName(), "unknown")) {
          rm.setName(FilenameUtils.removeExtension(templateFile.getName()));
        }
        return rm;
      } catch (IOException e) {
        throw new IllegalArgumentException(
            "Error encountered in processing the template" + templateFile, e);
      }
    } else {
      throw new IllegalArgumentException("File not present:" + templateFile);
    }

  }

  public static ResourceModelReader getInstance() {
    if (reader == null) {
      reader = new ResourceModelReader();
    }
    return reader;
  }



}
