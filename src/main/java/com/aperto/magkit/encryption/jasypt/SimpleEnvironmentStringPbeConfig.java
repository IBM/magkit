package com.aperto.magkit.encryption.jasypt;

import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;

/**
 * A simple extension of EnvironmentStringPBEConfig that tries to hide the encryption key from users
 * that don't know how to decompile classes - just a week obfruscation of the key phrase.
 *
 * @author wolf.bubenik
 * @since 02.07.2009
 */
public class SimpleEnvironmentStringPbeConfig extends EnvironmentStringPBEConfig {

    public SimpleEnvironmentStringPbeConfig() {
        setPassword("!easy-to-guess-encryption-key-4-aperto!");
    }
}
