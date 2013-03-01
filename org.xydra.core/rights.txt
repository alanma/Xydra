XydraStore
  setAdminAccount( XId account, String passwordHash )
  
  Implementierung kann AdminAccount haben
  
  In Server: 
    Setzen über web.xml
  
  
  
Admin Account
  DARF ALLES
    accounts erstellen
    zu gruppen hinzufügen
    alles lesen/schreiben
    


=== Namespaces

Constants:
* 'internal--accounts'  default model for accounts, created by repo impl
* 'internal--rights-+{modelId}' ...  

* 'internal--index-'+{modelId} .... erbt rechts vom entsprechenden model
** cannot be read/written from outside
--> let the impl handl this

Model "phonebook"
  Daten

Model "internal--rights-phonebook"
  Wer darf "phonebook" ändern?
  Wer darf "rechte-phonebook" lesen oder ändern?
      
Model "internal--accounts"
  Acounts
  Gruppen
  Wer ist in welcher Gruppe?
   
Model "internal--rights-internal--accounts"
  Wer darf acounts anlegen?
  Wer darf Gruppen ändern?
  ... 
  
  Wer darf auf "internal-rights-accounts" zugreifen (lesen/schreiben)?
  
  
=== Algo für Anlegen eines Models
Wer darf initial lesen/schreiben?
Wer setzt diese Rechte?

Policy
  Wer ein Model anlegt, dem gehört es allein
  