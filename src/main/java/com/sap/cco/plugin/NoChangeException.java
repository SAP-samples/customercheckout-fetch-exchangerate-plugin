/*
 * Copyright (c) 2024. SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cco.plugin;

public class NoChangeException extends Exception {

    public NoChangeException() {
        super("Don't change the corresponding currency value");
    }
}
