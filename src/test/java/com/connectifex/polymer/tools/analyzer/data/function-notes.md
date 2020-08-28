Functions: 32 -> 45 when allow void returns (generally validation functions that abort())

asGroovyList  -  returns:List
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-output-1.1.0.groovy

Doesn't appear to be used


checkPonAid  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-output-1.1.0.groovy

Combination of pattern with subsequent checking of ranges on some values
So - need to be able to validate variables that have been split from a pattern

        void checkPonAid(String ponAid) {
                String [] tokens = ponAid.split("-");
                if(tokens.length != 4)
                        throw new RuntimeException("PonAid is not in the correct format " + ponAid)
                int shelf = Integer.valueOf(tokens[1])
                int slot = Integer.valueOf(tokens[2])
                int port = Integer.valueOf(tokens[3])
                if(shelf < 2 || shelf > 5)
                        throw new RuntimeException("PonAid shelf must be between 2 and 5 " + ponAid)
                if(slot < 1 || slot > 2)
                        throw new RuntimeException("PonAid slot must be between 1 and 2 " + ponAid)
                if(port < 1 || port > 16)
                                throw new RuntimeException("PonAid port must be between 1 and 16 " + ponAid)
        }

checkPonId  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-output-1.1.0.groovy

Validation for length of a variable

        void checkPonId(String ponId) {
                if( ponId.length() > 8 )
                        throw new RuntimeException("PonId is not in the correct format " + ponId)
        }


computeIPs  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-update-output-1.1.0.groovy

This is not actually being used

        void computeIPs(Map inputs, Map outputs) {
                String ip = inputs['static-ipv4']
                int count = 1
                if(inputs['static-ipv4-count'] != null)
                   count = Integer.parseInt(inputs['static-ipv4-count'])

                String [] tokens = ip.split(Pattern.quote("."));
                if(tokens.length != 4)
                        throw new RuntimeException("Invalid IP address");
                int lastPart = Integer.parseInt(tokens[3]);
                if(lastPart + count >= 255)
                        throw new RuntimeException("Invalid IP Count");
                String network = ip.substring(0,ip.lastIndexOf("."));
                int j = 0
                for(int i = lastPart; i< lastPart + count; i++) {
                        String newIp = network+"." + i;
                        outputs["ipv4-addr[$j]"] = newIp
                        j++
                }
        }


fillInEtherPorts  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-port-read-response-output-1.1.0.groovy

This appears to be cycling through a potential number of ports - seeing if they exist and then creating separate array indexed values
out of string based values

        int fillInEtherPorts(Map inputs, Map outputs) {
                int index = 0
                for(int i = 0 ; i < ether_ports; i++) {
                        int portNum = i + 1;
                        String name = inputs["name-$i"]
                        if(name != null && !name.trim().isEmpty())      {
                                outputs["port[$index]"] = "g" + Integer.toString(portNum)
                                outputs["port-type[$index]"] = inputs["port-type-$i"]
                                outputs["mac[$index]"] = inputs["mac-$i"]
                                outputs["speed[$index]"] = inputs["speed-$i"]
                                outputs["mtu[$index]"] = inputs["mtu-$i"]
                                outputs["admin-status[$index]"] = getAdminStatus(inputs["admin-state-$i"])
                                outputs["operational-status[$index]"] = getOperStatus(inputs["operational-state-$i"])
                                index++
                        }
                }
                return index
        }



fillInEtherPorts  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-port-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-port-read-output-1.1.0.groovy



        void fillInEtherPorts(Map inputs, Map outputs) {
                for(int i = 0 ; i < ether_ports; i++) {
                        int portNum = i + 1;
                        String port = inputs["shelf"] + "/" + inputs["slot"] + "/q" + Integer.toString(portNum)
                        outputs["etherport-encoded-$i"] = URLEncoder.encode(port, "UTF-8")
                }
        }


fillInPonPorts  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-port-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-port-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-port-read-response-output-1.1.0.groovy

        void fillInPonPorts(Map inputs, Map outputs) {
                for(int i = 0 ; i < pon_ports; i++) {
                        int portNum = i + 1;
                        String port = inputs["shelf"] + "/" + inputs["slot"] + "/xp" + Integer.toString(portNum)
                        outputs["ponport-encoded-$i"] = URLEncoder.encode(port, "UTF-8")
                }
        }


generateChannelTerminationId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-activate-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-create-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-replace-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-update-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-output-1.1.0.groovy

Need an example of the input format - for ponAid
Looks like a straight converter function
May be able to use normalization

        String generateChannelTerminationId(String ponAid) {
                int startIndex = ponAid.indexOf("-");
                if(startIndex > 0 && startIndex < ponAid.length()-2) {
                        String value =  ponAid.substring(startIndex+1).replaceAll("-","/")
                        int lastIndex = value.lastIndexOf("/");
                        if(lastIndex != -1 || lastIndex+1 < value.length()) {
                                StringBuilder builder = new StringBuilder();
                                String termId= builder.append(value.substring(0, lastIndex+1))
                                                .append("xp").append(value.substring(lastIndex+1, value.length())).toString();
                                return termId;
                        }
                        throw new RuntimeException("PonAid is not in the correct format")
                }
                throw new RuntimeException("PonAid is not in the correct format")
        }



generateCircuitPack  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-notificationretrieve-alarms-read-response-output-1.1.0.groovy

    String generateCircuitPack(String address){
        if (address.indexOf("ont") == -1)
            return ""
        else
        {
            address=address.split("=")[1].replaceAll("'","")
            return address.split("-")[1]
        }

    }


generateEquipmentType  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-notificationretrieve-alarms-read-response-output-1.1.0.groovy

        String generateEquipmentType(String address){
        if (address.indexOf("ont") == -1)
            return "OLT"
        else
            return "ONT"
    	   }

generatePonAId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ont-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-slot-read-response-output-1.1.0.groovy

    String generatePonAId(String ponAid) {
       if(ponAid == null)
                   return ""
        String modPortValue = ponAid.replace("/xp", "/");
        String modPonAidValue = modPortValue.replaceAll("/", "-")
        String modPonAidWithRackValue = "1-".concat(modPonAidValue)
        return modPonAidWithRackValue
    }


generatePonSystemId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-notificationretrieve-alarms-read-response-output-1.1.0.groovy

    String generatePonSystemId(String address){
        if (address.indexOf("ont") == -1)
            return ""
        else
        {
            address=address.split("=")[1].replaceAll("'","")
            return address.split("-")[0]
        }

    }


generateProfileId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-activate-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-create-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-replace-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-update-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy

Has data that should be initialized once and we're doing a lookup i.e. string to string look up
With a default value for non-existent mappings
If default not provided, abort()

        String generateProfileId(Map inputs) {
                def serviceMap = ["SFU-FIOS-RESIDENTIAL": "812NG-V", "SFU-FIOS-BUSINESS" : "GP1000X",
                                                        "SOHO-FIOS-BUSINESS": "862NG-V","IBONT-SES-BUSINESS":"882NG-V",
                                                        "SFU-RESIDENTIAL":"SFU", "SFU-BUSINESS":"SFU"]

                String key = inputs["ont-profile"] + "-" + inputs["pon-service-type"]
                return serviceMap.get(key,"812NG-V");
        }


getAdminStatus  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-ethernet-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-pon-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontport-update-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-ethernet-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-port-read-response-output-1.1.0.groovy

        String getAdminStatus(String adminState) {
            if(adminState == null || adminState.trim().isEmpty()) {
                    return "UNKNOWN"
            }
            if(adminState.trim().equalsIgnoreCase("up") || adminState.trim().equalsIgnoreCase("enable") ) {

                     return "UP"
            }
            else
                    return "DOWN"
        }


getBoolean  -  returns:boolean
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy


getConfigIndex  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy


getCpiId  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-slot-read-response-output-1.1.0.groovy


getCurrCommittedAct  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-read-response-output-1.1.0.groovy


getCurrCommittedPlanned  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-read-response-output-1.1.0.groovy


getDeviceName  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-create-output-1.0.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/cisco/1.1.0/epnm-cisco-circuits-eline-create-output-1.1.0.groovy


getInterfaceName  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-cir-create-output-1.0.0.groovy


getMobilityIndex  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-slot-read-response-output-1.1.0.groovy


getOntStatus  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy


getOperStatus  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-ethernet-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-pon-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-ethernet-port-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-port-read-response-output-1.1.0.groovy


getPartitionName  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-output-1.1.0.groovy


getPort  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontport-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontport-read-output-1.1.0.groovy


getPortId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy


getProfileId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy


getPst  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-read-response-output-1.1.0.groovy


getRequestIndex  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/cisco/1.1.0/epnm-cisco-circuits-eline-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-recreate-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-update-output-1.1.0.groovy


getSerialNumber  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-byserialnum-reset-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-all-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-byserialnum-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-unlocked-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ont-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-all-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-byserialnum-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-unlocked-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-read-response-output-1.1.0.groovy


getServiceCpi  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-slot-read-response-output-1.1.0.groovy


getServiceFacadeId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-cir-create-output-1.0.0.groovy


getServiceType  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy


getStatus  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-shelf-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-byserialnum-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ont-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-byserialnum-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-read-response-output-1.1.0.groovy


getVendorId  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ont-byserialnum-reset-output-1.1.0.groovy


getchannelPartitionIndex  -  returns:int
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-slot-read-response-output-1.1.0.groovy


ignoreUnused  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-cir-create-output-1.0.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-create-output-1.0.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-delete-cir-output-1.0.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-notificationretrieve-alarms-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-all-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-unlocked-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-all-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponport-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-unlocked-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-cpi-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-delete-vlan-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-cpi-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-cpi-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-delete-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-delete-tps-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-slot-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-child-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ponsystem-update-output-1.1.0.groovy


manipuateCommand  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-ponsystem-ont-read-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-ponsystem-ont-read-output-1.1.0.groovy


mapControlPolicy  -  returns:String
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-create-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-pondataservice-update-output-1.1.0.groovy


matchPonSystem  -  returns:boolean
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-ontretrieve-byserialnum-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-ontretrieve-byserialnum-read-response-output-1.1.0.groovy


populateEndPoint  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-ut-schemas/scripts/cartographer/morphers/ciena/1.0.0/bpmcp-ciena-circuits-eline-cir-create-output-1.0.0.groovy


popupalateCards  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/bnc-nc-schemas/scripts/cartographer/morphers/calix/1.1.0/odl-calix-olt-read-response-output-1.1.0.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/calix/1.1.0/odl-calix-ops-olt-read-response-output-1.1.0.groovy


splitArrayToList  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/cisco/6.4/odl-cisco-ops-acl-read-response-output-6.4.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/cisco/6.4/odl-cisco-ops-software-read-response-output-6.4.groovy


splitToList  -  returns:void
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/cisco/6.4/odl-cisco-ops-acl-read-response-output-6.4.groovy
/Users/peter/Downloads/von-bnc-nc-neon/lumina-bnc-nc-ops-schemas/src/main/resources/morphers/cisco/6.4/odl-cisco-ops-software-read-response-output-6.4.groovy


