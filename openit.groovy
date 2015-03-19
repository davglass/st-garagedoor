/**
 *  Is It Open?
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
    name: "Is It Open?",
    namespace: "gbknet",
    author: "Greg Bronzert",
    description: "Check whether door is closed after a mode change and open it.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section("Which mode change triggers the check?") {
        input "newMode", "mode", title: "Which?", multiple: true, required: true
    }
    section("Which door should I open?"){
        input "door", "capability.doorControl", title: "Which Door?", multiple: false, required: true
    }
    section("But only if it's from this mode:") {
        input "fromMode", "mode", title: "Which?", multiple: false, required: true
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
}


def modeChangeHandler(evt) {
    log.debug "Mode change to: ${evt.value} from ${state.lastMode}"

    if (state.lastMode == fromMode) {
        if (newMode.any{ it == evt.value } || newMode == evt.value) {
            checkDoor()
        }
    }
        
    if (!state.lastMode) {
        state.lastMode = evt.value
    }
    log.trace state
}

def checkDoor() {
    log.debug "Door ${door.displayName} is ${door.currentContact}"
    if (door.currentContact == "closed") {
        def msg = "${door.displayName} is closed, opening it!"
        log.info msg
        sendNotificationEvent(msg)
        openDoor();
    } else {
        def msg = "${door.displayName} was already open, we are good."
        log.debug msg
        sendNotificationEvent(msg)
    }
}

private openDoor() {
    if (door.currentContact == "closed") {
        log.debug "opening door"
        door.open()
    }
}