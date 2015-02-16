package org.openepics.names.services;

import org.openepics.names.model.NamePartType;
import org.openepics.names.util.NotImplementedException;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;

import java.util.List;

/**
 * An empty stub for the naming convention used by FRIB.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Alternative
@Stateless
public class FribNamingConvention implements NamingConvention {

    @Override public boolean isSectionNameValid(List<String> parentPath, String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isDeviceTypeNameValid(List<String> parentPath, String name) {
        throw new NotImplementedException();
    }

    @Override public boolean isInstanceIndexValid(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }

    @Override public String equivalenceClassRepresentative(String name) {
        throw new NotImplementedException();
    }

    @Override public String conventionName(List<String> sectionPath, List<String> deviceTypePath, @Nullable String instanceIndex) {
        throw new NotImplementedException();
    }

    @Override public boolean canMnemonicsCoexist(List<String> newMnemonicPath, NamePartType newMnemonicType, List<String> comparableMnemonicPath, NamePartType comparableMnemonicType) {
        throw new NotImplementedException();
    }

	@Override
	public String deviceDefinition(List<String> deviceTypePath) {
		throw new NotImplementedException();
	}

	@Override
	public String areaName(List<String> sectionPath) {
		throw new NotImplementedException();
	}
}
