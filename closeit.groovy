/**
 *  Is It Closed?
 *
 *  Copyright 2014 Greg Bronzert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Is It Closed?",
    namespace: "gbknet",
    author: "Greg Bronzert",
    description: "Check whether door is closed after a mode change or specific time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section("Which mode changes trigger the check?") {
        input "newMode", "mode", title: "Which?", multiple: true, required: false
    }
    section("When should I check? (once per day)") {
        input "timeToCheck", "time", title: "(Optional)", required: false
    }
    section("Which doors should I check?"){
        input "doors", "capability.doorControl", title: "Which?", multiple: true, required: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    if (newMode != null) {
        subscribe(location, modeChangeHandler)
    }
    if (timeToCheck != null) {
        schedule(timeToCheck, checkDoor)
    }
}

def modeChangeHandler(evt) {
    log.debug "Mode change to: ${evt.value}"

    doors.each {
        if (newMode.any{ it == evt.value } || newMode == evt.value) {
            checkDoor(it)
        }
    }
    
}

def checkDoor(door) {
    log.debug "Door ${door.displayName} is ${door.currentContact}"
    if (door.currentContact == "open") {
        def msg = "${door.displayName} was left open, closing it!"
        log.info msg
        sendNotificationEvent(msg)
        closeDoor(door);
    } else {
        def msg = "${door.displayName} was not open, we are good."
        log.debug msg
        sendNotificationEvent(msg)
    }
}

private closeDoor(door) {
    if (door.currentContact == "open") {
        log.debug "closing door"
        door.close()
    }
}
