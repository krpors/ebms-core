package nl.clockwork.ebms.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class EbMSKeyStore
{
	@NonNull
	String path;
	@NonNull
	KeyStore keyStore;
	@NonNull
	String keyPassword;
	String defaultAlias;

	public EbMSKeyStore(@NonNull KeyStoreType type, @NonNull String path, @NonNull String password, @NonNull String keyPassword) throws GeneralSecurityException, IOException
	{
		this(type,path,password,keyPassword,null);
	}

	public EbMSKeyStore(@NonNull KeyStoreType type, @NonNull String path, @NonNull String password, @NonNull String keyPassword, String defaultAlias) throws GeneralSecurityException, IOException
	{
		this.path = path;
		this.keyPassword = keyPassword;
		this.defaultAlias = defaultAlias;
		this.keyStore = KeyStoreUtils.loadKeyStore(type,path,password);
	}

	public Certificate getCertificate(String alias) throws KeyStoreException
	{
		return keyStore.getCertificate(alias);
	}

	public String getCertificateAlias(X509Certificate cert) throws KeyStoreException
	{
		return keyStore.getCertificateAlias(cert);
	}

	public Certificate[] getCertificateChain(String alias) throws KeyStoreException
	{
		return keyStore.getCertificateChain(alias);
	}

	public Key getKey(String alias, char[] password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException
	{
		return keyStore.getKey(alias,password);
	}

}
