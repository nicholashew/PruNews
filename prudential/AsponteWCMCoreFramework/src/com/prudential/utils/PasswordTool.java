/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.utils;

public class PasswordTool {
	
	  public static final String STRING_CONVERSION_CODE = "UTF8";
	  public static final String DEFAULT_CRYPTO_ALGORITHM;
	  private static final String EMPTY_STRING = new String("");
	  private static final String[] SUPPORTED_CRYPTO_ALGORITHMS;
	  private static final byte[] BASE64_ENCODE_MAP;
	  private static final byte[] BASE64_DECODE_MAP;
	  private static String[] _supported_crypto_algorithms ={ "xor", "os400", "custom" };
	  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	  


	  static
	  {
	    SUPPORTED_CRYPTO_ALGORITHMS = getSupportedCryptoAlgorithms();
	    DEFAULT_CRYPTO_ALGORITHM = SUPPORTED_CRYPTO_ALGORITHMS[0];

	    byte[] map = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };

	    BASE64_ENCODE_MAP = map;
	    BASE64_DECODE_MAP = new byte[''];

	    for (int idx = 0; idx < BASE64_DECODE_MAP.length; idx++)
	    {
	      BASE64_DECODE_MAP[idx] = -1;
	    }

	    for (int idx = 0; idx < BASE64_ENCODE_MAP.length; idx++)
	    {
	      BASE64_DECODE_MAP[BASE64_ENCODE_MAP[idx]] = ((byte)idx);
	    }
	  }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String encoded_string = "{xor}CDo9Hgw=";
		String decoded_string = decode_password(removeCryptoAlgorithmTag(encoded_string), DEFAULT_CRYPTO_ALGORITHM);
		System.out.println("This is the decoded string:"+decoded_string);

	}
	
	


	  public static String removeCryptoAlgorithmTag(String encoded_string)
	  {
	    String encoded_password = null;

	    if (encoded_string != null)
	    {
	      encoded_string = encoded_string.trim();

	      if (encoded_string.length() >= 2)
	      {
	        int algorithm_started = encoded_string.indexOf("{");

	        if (algorithm_started == 0)
	        {
	          int algorithm_stopped = encoded_string.indexOf("}", ++algorithm_started);

	          if (algorithm_stopped > 0)
	          {
	            algorithm_stopped++; if (algorithm_stopped < encoded_string.length())
	            {
	              encoded_password = encoded_string.substring(algorithm_stopped).trim();
	            }
	            else
	            {
	              encoded_password = EMPTY_STRING;
	            }
	          }
	        }
	      }
	    }

	    return encoded_password;
	  }

	  public static String[] getSupportedCryptoAlgorithms()
	  {
	    return _supported_crypto_algorithms;
	  }
	  
	  private static String decode_password(String encoded_string, String crypto_algorithm)
	  {
	    StringBuffer buffer = new StringBuffer();

	    if (crypto_algorithm.length() == 0)
	    {
	      buffer.append(encoded_string);
	    }
	    else
	    {
	      String decoded_string = null;

	      if (encoded_string.length() > 0)
	      {
	        byte[] encrypted_bytes = convert_viewable_to_bytes(encoded_string);

	        if (encrypted_bytes == null)
	        {
	          return null;
	        }

	        if (encrypted_bytes.length > 0)
	        {
	          byte[] decrypted_bytes = null;
	          try
	          {
	            decrypted_bytes = decipher(encrypted_bytes, crypto_algorithm);
	          }
	          catch (Exception e)
	          {
	            e.printStackTrace();
	            return null;
	          }

	          if ((decrypted_bytes != null) && (decrypted_bytes.length > 0))
	          {
	            decoded_string = convert_to_string(decrypted_bytes);
	          }
	        }
	      }

	      if ((decoded_string != null) && (decoded_string.length() > 0))
	      {
	        buffer.append(decoded_string);
	      }
	    }

	    return buffer.toString();
	  }
	  
	  private static byte[] convert_viewable_to_bytes(String string)
	  {
	    byte[] bytes = null;

	    if (string != null)
	    {
	      if (string.length() == 0)
	      {
	        bytes = EMPTY_BYTE_ARRAY;
	      }
	      else
	      {
	        try
	        {
	          bytes = base64Decode(convert_to_bytes(string));
	        }
	        catch (Exception e)
	        {
	         e.printStackTrace();
	         bytes = null;
	        }
	      }

	    }

	    return bytes;
	  }
	  
	  private static byte[] convert_to_bytes(String string)
	  {
	    byte[] bytes = null;

	    if (string != null)
	    {
	      if (string.length() == 0)
	      {
	        bytes = EMPTY_BYTE_ARRAY;
	      }
	      else
	      {
	        try
	        {
	          bytes = string.getBytes("UTF8");
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	          bytes = null;
	        }
	      }
	    }

	    return bytes;
	  }
	  
	  private static String convert_to_string(byte[] bytes)
	  {
	    String string = null;

	    if (bytes != null)
	    {
	      if (bytes.length == 0)
	      {
	        string = EMPTY_STRING;
	      }
	      else
	      {
	        try
	        {
	          string = new String(bytes, "UTF8");
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	          string = null;
	        }
	      }
	    }

	    return string;
	  }
	  
	  private static byte[] base64Decode(byte[] bytes)
	  {
	    int tail = bytes.length;

	    while (bytes[(--tail)] == 61);
	    byte[] dest = new byte[tail + 1 - bytes.length / 4];

	    for (int idx = 0; idx < bytes.length; idx++)
	    {
	      bytes[idx] = BASE64_DECODE_MAP[bytes[idx]];
	    }

	    int destx = dest.length - 2;
	    int didx = 0;
	    int sidx = 0;

	    while (didx < destx)
	    {
	      dest[didx] = ((byte)(bytes[sidx] << 2 & 0xFF | bytes[(sidx + 1)] >>> 4 & 0x3));
	      dest[(didx + 1)] = ((byte)(bytes[(sidx + 1)] << 4 & 0xFF | bytes[(sidx + 2)] >>> 2 & 0xF));
	      dest[(didx + 2)] = ((byte)(bytes[(sidx + 2)] << 6 & 0xFF | bytes[(sidx + 3)] & 0x3F));
	      didx += 3;
	      sidx += 4;
	    }

	    if (didx < dest.length)
	    {
	      dest[(didx++)] = ((byte)(bytes[sidx] << 2 & 0xFF | bytes[(sidx + 1)] >>> 4 & 0x3));

	      if (didx < dest.length)
	      {
	        dest[didx] = ((byte)(bytes[(sidx + 1)] << 4 & 0xFF | bytes[(sidx + 2)] >>> 2 & 0xF));
	      }
	    }

	    return dest;
	  }
	  
	  public static byte[] decipher(byte[] encrypted_bytes, String crypto_algorithm)
			    throws Exception {
			    
		    byte[] decrypted_bytes = null;
		    decrypted_bytes = xor(encrypted_bytes);

			    if (decrypted_bytes == null)
			    {
			      throw new Exception();
			    }

			    return decrypted_bytes;
			  }
	  private static byte[] xor(byte[] bytes)
	  {
	    byte[] xor_bytes = null;

	    if (bytes != null)
	    {
	      xor_bytes = new byte[bytes.length];

	      for (int i = 0; i < bytes.length; i++)
	      {
	        xor_bytes[i] = ((byte)(0x5F ^ bytes[i]));
	      }
	    }

	    return xor_bytes;
	  }

}
