tiltakspenger
================

Innhenter informasjon om uføretrygd for en søker av tiltakspenger.

En del av satsningen ["Flere i arbeid – P4"](https://memu.no/artikler/stor-satsing-skal-fornye-navs-utdaterte-it-losninger-og-digitale-verktoy/)

# Komme i gang

## Forutsetninger

- [JDK](https://jdk.java.net/)
- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/) brukes som byggeverktøy og er inkludert i oppsettet

For hvilke versjoner som brukes, [se byggefilen](build.gradle.kts)

## Bygging og denslags

For å bygge artifaktene:

```sh
./gradlew build
```

# Tolkning av svar fra Pesys

En utviklers tolkning, med bistand fra fagkyndig jurist.

Gyldig forespørsel til Pesys (fnr settes som en header):

```
http://<pesysURL>/harUforegrad?fom=2022-01-01&tom=2022-12-31&uforeTyper=UFORE&uforeTyper=UF_M_YRKE
```

Pesys svarer

- 200 dersom fnr finnes i Pesys. Vi svarer det Pesys svarer
- 400 ved Bad Request som f.eks. ugyldig fnr eller dato. Vi kaster Exception
- 404 dersom fnr ikke finnes i Pesys. Vi svarer `harUforegrad=false`
- 5xx og andre feilkoder. Vi kaster Exception

Det finnes flere `uforeTyper`, men ved å bruke `UFORE` (Uføre) og `UF_M_YRKE` (Uføre m/yrkesskade) vil man treffe de
periodene med uføregrad som finnes, ifølge Pesys

Eksempel på 200-svar fra Pesys ved gyldig input:

```json
{
  "harUforegrad": true,
  "datoUfor": "2022-02-01",
  "virkDato": "2022-09-01"
}
```

- om man spør om en periode _før_ `datoUfor`, får man `harUforegrad=false`
- om man spør om en periode der `datoUfor` _inngår_, får man `harUforegrad=true`
- om man spør om en periode _etter_ `datoUfor`, får man `harUforegrad=true` (fordi man ikke vet når ufør opphører)

`datoUfor` virker ikke være relevant for oss. Det er derimot `virkDato`, virkningstidspunkt. For bruker er dette den dagen penger blir utbetalt.

Man kan spørre seg hvorfor ikke `datoUfor` er relevant og hvordan det kan være forskjell på `datoUfor` og `virkDato`.
Mest sannsynlig går brukeren da på en annen ytelse (f.eks. AAP) mellom 2022-02-01 og 2022-09-01 gitt eksempelsvar over.
Tiltakspengeforskriften, § 7 Forholdet til andre ytelser, sier

> Det gis ikke tiltakspenger for samme periode som tiltaksdeltakeren har rett til andre ytelser til livsopphold

Gitt det ovenstående, vil logikken som avgjør om vedkommende har en uføregrad innenfor gitt periode være

```kotlin
val vilkårOppfylt = harUforegrad && virkDato.before(tom)
```

Eventuelt kunne man tenke seg følgende logikk, men den er ikke like intuitiv

```kotlin
val vilkårOppfylt = (virkDato != null) && virkDato.before(tom)
```

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #tpts-tech.
