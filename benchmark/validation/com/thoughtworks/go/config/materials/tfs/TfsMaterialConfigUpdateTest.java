/**
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.config.materials.tfs;


import AbstractMaterialConfig.MATERIAL_NAME;
import ScmMaterialConfig.AUTO_UPDATE;
import ScmMaterialConfig.FILTER;
import ScmMaterialConfig.FOLDER;
import ScmMaterialConfig.PASSWORD;
import ScmMaterialConfig.URL;
import ScmMaterialConfig.USERNAME;
import TfsMaterialConfig.DOMAIN;
import TfsMaterialConfig.PASSWORD_CHANGED;
import TfsMaterialConfig.PROJECT_PATH;
import com.thoughtworks.go.config.CaseInsensitiveString;
import com.thoughtworks.go.config.ConfigSaveValidationContext;
import com.thoughtworks.go.config.materials.IgnoredFiles;
import com.thoughtworks.go.security.CryptoException;
import com.thoughtworks.go.security.GoCipher;
import com.thoughtworks.go.util.ReflectionUtil;
import com.thoughtworks.go.util.command.UrlArgument;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class TfsMaterialConfigUpdateTest {
    @Test
    public void shouldSetConfigAttributes() {
        TfsMaterialConfig tfsMaterialConfig = new TfsMaterialConfig(new GoCipher(), new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "some_domain", "passwd", "walk_this_path");
        Map<String, String> map = new HashMap<>();
        map.put(URL, "http://foo:8080/tfs/HelloWorld");
        map.put(USERNAME, "boozer");
        map.put(PASSWORD, "secret");
        map.put(FOLDER, "folder");
        map.put(AUTO_UPDATE, "0");
        map.put(FILTER, "/root,/**/*.help");
        map.put(MATERIAL_NAME, "my-tfs-material-name");
        map.put(PROJECT_PATH, "/useless/project");
        map.put(DOMAIN, "CORPORATE");
        tfsMaterialConfig.setConfigAttributes(map);
        TfsMaterialConfig newTfsMaterialConfig = new TfsMaterialConfig(new GoCipher(), new UrlArgument("http://foo:8080/tfs/HelloWorld"), "boozer", "CORPORATE", "secret", "/useless/project");
        newTfsMaterialConfig.setName(new CaseInsensitiveString("my-tfs-material-name"));
        newTfsMaterialConfig.setFolder("folder");
        Assert.assertThat(tfsMaterialConfig, Matchers.is(newTfsMaterialConfig));
        Assert.assertThat(tfsMaterialConfig.getPassword(), Matchers.is("passwd"));
        Assert.assertThat(tfsMaterialConfig.isAutoUpdate(), Matchers.is(false));
        Assert.assertThat(tfsMaterialConfig.getDomain(), Matchers.is("CORPORATE"));
        Assert.assertThat(tfsMaterialConfig.getName(), Matchers.is(new CaseInsensitiveString("my-tfs-material-name")));
        Assert.assertThat(tfsMaterialConfig.filter(), Matchers.is(new com.thoughtworks.go.config.materials.Filter(new IgnoredFiles("/root"), new IgnoredFiles("/**/*.help"))));
    }

    @Test
    public void shouldDefaultDomainToEmptyStringWhenNothingIsSet() throws Exception {
        TfsMaterialConfig tfsMaterialConfig = new TfsMaterialConfig(Mockito.mock(GoCipher.class));
        Assert.assertThat(tfsMaterialConfig.getDomain(), Matchers.is(""));
    }

    @Test
    public void setConfigAttributes_shouldUpdatePasswordWhenPasswordChangedBooleanChanged() throws Exception {
        TfsMaterialConfig tfsMaterialConfig = new TfsMaterialConfig(new GoCipher(), new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "passwd", "walk_this_path");
        Map<String, String> map = new HashMap<>();
        map.put(TfsMaterialConfig.PASSWORD, "secret");
        map.put(PASSWORD_CHANGED, "1");
        tfsMaterialConfig.setConfigAttributes(map);
        tfsMaterialConfig.setConfigAttributes(map);
        Assert.assertThat(ReflectionUtil.getField(tfsMaterialConfig, "password"), Matchers.is(Matchers.nullValue()));
        Assert.assertThat(tfsMaterialConfig.getPassword(), Matchers.is("secret"));
        Assert.assertThat(tfsMaterialConfig.getEncryptedPassword(), Matchers.is(new GoCipher().encrypt("secret")));
        // Dont change
        map.put(TfsMaterialConfig.PASSWORD, "Hehehe");
        map.put(PASSWORD_CHANGED, "0");
        tfsMaterialConfig.setConfigAttributes(map);
        Assert.assertThat(ReflectionUtil.getField(tfsMaterialConfig, "password"), Matchers.is(Matchers.nullValue()));
        Assert.assertThat(tfsMaterialConfig.getPassword(), Matchers.is("secret"));
        Assert.assertThat(tfsMaterialConfig.getEncryptedPassword(), Matchers.is(new GoCipher().encrypt("secret")));
        map.put(TfsMaterialConfig.PASSWORD, "");
        map.put(PASSWORD_CHANGED, "1");
        tfsMaterialConfig.setConfigAttributes(map);
        Assert.assertThat(tfsMaterialConfig.getPassword(), Matchers.is(Matchers.nullValue()));
        Assert.assertThat(tfsMaterialConfig.getEncryptedPassword(), Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void validate_shouldEnsureMandatoryFieldsAreNotBlank() {
        TfsMaterialConfig tfsMaterialConfig = new TfsMaterialConfig(new GoCipher(), new UrlArgument(""), "", "CORPORATE", "", "");
        tfsMaterialConfig.validate(new ConfigSaveValidationContext(null));
        Assert.assertThat(tfsMaterialConfig.errors().on(TfsMaterialConfig.URL), Matchers.is("URL cannot be blank"));
        Assert.assertThat(tfsMaterialConfig.errors().on(TfsMaterialConfig.USERNAME), Matchers.is("Username cannot be blank"));
        Assert.assertThat(tfsMaterialConfig.errors().on(PROJECT_PATH), Matchers.is("Project Path cannot be blank"));
    }

    @Test
    public void validate_shouldEnsureMaterialNameIsValid() {
        TfsMaterialConfig tfsMaterialConfig = new TfsMaterialConfig(new GoCipher(), new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "passwd", "walk_this_path");
        tfsMaterialConfig.validate(new ConfigSaveValidationContext(null));
        Assert.assertThat(tfsMaterialConfig.errors().on(TfsMaterialConfig.MATERIAL_NAME), Matchers.is(Matchers.nullValue()));
        tfsMaterialConfig.setName(new CaseInsensitiveString(".bad-name-with-dot"));
        tfsMaterialConfig.validate(new ConfigSaveValidationContext(null));
        Assert.assertThat(tfsMaterialConfig.errors().on(TfsMaterialConfig.MATERIAL_NAME), Matchers.is("Invalid material name '.bad-name-with-dot'. This must be alphanumeric and can contain underscores and periods (however, it cannot start with a period). The maximum allowed length is 255 characters."));
    }

    @Test
    public void validate_shouldEnsureDestFilePathIsValid() {
        TfsMaterialConfig tfsMaterialConfig = new TfsMaterialConfig(new GoCipher(), new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "passwd", "walk_this_path");
        tfsMaterialConfig.setConfigAttributes(Collections.singletonMap(FOLDER, "../a"));
        tfsMaterialConfig.validate(new ConfigSaveValidationContext(null));
        Assert.assertThat(tfsMaterialConfig.errors().on(TfsMaterialConfig.FOLDER), Matchers.is("Dest folder '../a' is not valid. It must be a sub-directory of the working folder."));
    }

    @Test
    public void shouldThrowErrorsIfBothPasswordAndEncryptedPasswordAreProvided() {
        TfsMaterialConfig materialConfig = new TfsMaterialConfig(new UrlArgument("foo/bar"), "password", "encryptedPassword", new GoCipher());
        materialConfig.validate(new ConfigSaveValidationContext(null));
        Assert.assertThat(materialConfig.errors().on("password"), Matchers.is("You may only specify `password` or `encrypted_password`, not both!"));
        Assert.assertThat(materialConfig.errors().on("encryptedPassword"), Matchers.is("You may only specify `password` or `encrypted_password`, not both!"));
    }

    @Test
    public void shouldValidateWhetherTheEncryptedPasswordIsCorrect() {
        TfsMaterialConfig materialConfig = new TfsMaterialConfig(new UrlArgument("foo/bar"), "", "encryptedPassword", new GoCipher());
        materialConfig.validate(new ConfigSaveValidationContext(null));
        Assert.assertThat(materialConfig.errors().on("encryptedPassword"), Matchers.is("Encrypted password value for TFS material with url 'foo/bar' is invalid. This usually happens when the cipher text is modified to have an invalid value."));
    }

    @Test
    public void shouldEncryptTfsPasswordAndMarkPasswordAsNull() throws Exception {
        GoCipher mockGoCipher = Mockito.mock(GoCipher.class);
        Mockito.when(mockGoCipher.encrypt("password")).thenReturn("encrypted");
        Mockito.when(mockGoCipher.maybeReEncryptForPostConstructWithoutExceptions("encrypted")).thenReturn("encrypted");
        TfsMaterialConfig materialConfig = new TfsMaterialConfig(mockGoCipher, new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "password", "walk_this_path");
        materialConfig.ensureEncrypted();
        Assert.assertThat(materialConfig.getPassword(), Matchers.is(Matchers.nullValue()));
        Assert.assertThat(materialConfig.getEncryptedPassword(), Matchers.is("encrypted"));
    }

    @Test
    public void shouldDecryptTfsPassword() throws Exception {
        GoCipher mockGoCipher = Mockito.mock(GoCipher.class);
        Mockito.when(mockGoCipher.decrypt("encrypted")).thenReturn("password");
        Mockito.when(mockGoCipher.maybeReEncryptForPostConstructWithoutExceptions("encrypted")).thenReturn("encrypted");
        TfsMaterialConfig materialConfig = new TfsMaterialConfig(mockGoCipher, new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "secret", "walk_this_path");
        ReflectionUtil.setField(materialConfig, "encryptedPassword", "encrypted");
        materialConfig.ensureEncrypted();
        Assert.assertThat(materialConfig.getPassword(), Matchers.is("password"));
    }

    @Test
    public void shouldNotDecryptTfsPasswordIfPasswordIsNotNull() throws Exception {
        GoCipher mockGoCipher = Mockito.mock(GoCipher.class);
        Mockito.when(mockGoCipher.encrypt("password")).thenReturn("encrypted");
        Mockito.when(mockGoCipher.decrypt("encrypted")).thenReturn("password");
        TfsMaterialConfig materialConfig = new TfsMaterialConfig(mockGoCipher, new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "password", "walk_this_path");
        materialConfig.ensureEncrypted();
        Mockito.when(mockGoCipher.encrypt("new_password")).thenReturn("new_encrypted");
        materialConfig.setPassword("new_password");
        Mockito.when(mockGoCipher.decrypt("new_encrypted")).thenReturn("new_password");
        Assert.assertThat(materialConfig.getPassword(), Matchers.is("new_password"));
    }

    @Test
    public void shouldErrorOutIfDecryptionFails() throws CryptoException {
        GoCipher mockGoCipher = Mockito.mock(GoCipher.class);
        String fakeCipherText = "fake cipher text";
        Mockito.when(mockGoCipher.decrypt(fakeCipherText)).thenThrow(new CryptoException("exception"));
        TfsMaterialConfig materialConfig = new TfsMaterialConfig(mockGoCipher, new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "passwd", "walk_this_path");
        ReflectionUtil.setField(materialConfig, "encryptedPassword", fakeCipherText);
        try {
            materialConfig.getPassword();
            Assert.fail("Should have thrown up");
        } catch (Exception e) {
            Assert.assertThat(e.getMessage(), Matchers.is("Could not decrypt the password to get the real password"));
        }
    }

    @Test
    public void shouldErrorOutIfEncryptionFails() throws Exception {
        GoCipher mockGoCipher = Mockito.mock(GoCipher.class);
        Mockito.when(mockGoCipher.encrypt("password")).thenThrow(new CryptoException("exception"));
        try {
            new TfsMaterialConfig(mockGoCipher, new UrlArgument("http://10.4.4.101:8080/tfs/Sample"), "loser", "CORPORATE", "password", "walk_this_path");
            Assert.fail("Should have thrown up");
        } catch (Exception e) {
            Assert.assertThat(e.getMessage(), Matchers.is("Password encryption failed. Please verify your cipher key."));
        }
    }

    @Test
    public void shouldReturnTheUrl() {
        String url = "git@github.com/my/repo";
        TfsMaterialConfig config = new TfsMaterialConfig();
        config.setUrl(url);
        Assert.assertThat(config.getUrl(), Matchers.is(url));
    }

    @Test
    public void shouldReturnNullIfUrlForMaterialNotSpecified() {
        TfsMaterialConfig config = new TfsMaterialConfig();
        Assert.assertNull(config.getUrl());
    }

    @Test
    public void shouldHandleNullWhenSettingUrlForAMaterial() {
        TfsMaterialConfig config = new TfsMaterialConfig();
        config.setUrl(null);
        Assert.assertNull(config.getUrl());
    }
}
