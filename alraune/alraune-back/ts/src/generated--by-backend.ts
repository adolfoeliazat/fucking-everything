/*
 * (C) Copyright 2017 Vladimir Grechka
 *
 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
 */

//
// Generated on Tue May 30 05:30:58 GMT 2017
//

namespace AlBackToFrontCommand {

    export interface SayWarmFuckYou {
        opcode: "SayWarmFuckYou"
        toWhom: string
    }

    export interface SetClickHandler {
        opcode: "SetClickHandler"
        actions: Type[]
        targetDomid: string
    }

    export type Type = SayWarmFuckYou | SetClickHandler
}
