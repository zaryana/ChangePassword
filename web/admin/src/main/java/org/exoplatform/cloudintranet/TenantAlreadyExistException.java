package org.exoplatform.cloudintranet;

import org.exoplatform.cloudmanagement.admin.TenantValidationException;

public class TenantAlreadyExistException extends TenantValidationException
{

   /**
    * 
    */
   private static final long serialVersionUID = -683173664760298865L;

   public TenantAlreadyExistException(String message)
   {
      super(message);
      // TODO Auto-generated constructor stub
   }

}
