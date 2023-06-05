package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.LibraryContext
import dev.icerock.moko.kswift.plugin.feature.Filter
import dev.icerock.moko.kswift.plugin.feature.SealedToSwiftAssociatedEnumFeature
import io.outfoxx.swiftpoet.FileSpec
import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.resolveSingleFileKlib
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("LongMethod", "MaxLineLength")
class SealedToSwiftAssociatedEnumFeatureTest {
    @Test
    fun `associated enum feature should produce type mapped output`() {
        val klibPath = this::class.java.classLoader.getResource("associated-enum.klib")
        val konanFile = org.jetbrains.kotlin.konan.file.File(klibPath.toURI().path)
        // Need to use tooling strategy here since the klib was generated with 1.8
        // kotlinc-native TestingSealed.kt -p library -o associated-enum
        // or from project ./gradlew iosArm64MainKlibrary
        val library = resolveSingleFileKlib(
            libraryFile = konanFile,
            strategy = ToolingSingleFileKlibResolveStrategy,
        )
        val metadata = KlibModuleMetadata.read(KotlinMetadataLibraryProvider(library))
        val libraryContext = LibraryContext(metadata)
        val fileSpecBuilder = FileSpec.builder(moduleName = "module", fileName = "file")

        libraryContext.visit { featureContext ->
            (featureContext as? ClassContext)?.let {
                SealedToSwiftAssociatedEnumFeature(
                    featureContext = ClassContext::class,
                    filter = Filter.Exclude(emptySet()),
                ).doProcess(
                    featureContext = it,
                    fileSpecBuilder = fileSpecBuilder,
                    kotlinFrameworkName = "shared",
                )
            }
        }

        val appendable = InMemoryAppendable()
        fileSpecBuilder.build().writeTo(appendable)
        val expected = """import Foundation
import shared

/**
 * selector: ClassContext/My_Application:shared/dev/icerock/moko/kswift/plugin/associatedenum/LoadingState */
public enum LoadingStateKs<T : AnyObject, P : AnyObject> {

  case errorOnLoad(error: String)
  case loading
  case other(otherPayload: P)
  case success(payload: T?)

  public var sealed: LoadingState<T, P> {
    switch self {
    case .errorOnLoad(let error):
      return shared.LoadingStateErrorOnLoad<T, P>(error: error)
    case .loading:
      return shared.LoadingStateLoading<T, P>()
    case .other(let otherPayload):
      return shared.LoadingStateOther<T, P>(otherPayload: otherPayload)
    case .success(let payload):
      return shared.LoadingStateSuccess<T, P>(payload: payload)
    }
  }

  public init(_ obj: LoadingState<T, P>) {
    if let obj = obj as? shared.LoadingStateErrorOnLoad<T, P> {
      self = .errorOnLoad(error: obj.error as String)
    } else if obj is shared.LoadingStateLoading<T, P> {
      self = .loading
    } else if let obj = obj as? shared.LoadingStateOther<T, P> {
      self = .other(otherPayload: obj.otherPayload)
    } else if let obj = obj as? shared.LoadingStateSuccess<T, P> {
      self = .success(payload: obj.payload)
    } else {
      fatalError("LoadingStateKs not synchronized with LoadingState class")
    }
  }

}

/**
 * selector: ClassContext/My_Application:shared/dev/icerock/moko/kswift/plugin/associatedenum/TestingSealed */
public enum TestingSealedKs {

  case hasChar(mychar: Character)
  case hasEnum(myenum: OwnEnum)
  case hasFunction(myfunc: (
    KotlinInt,
    [KotlinBoolean],
    String
  ) -> String)
  case hasImmutableList(innerImmutableList: [String])
  case hasImmutableListNullable(innerImmutableListNullable: [String]?)
  case hasImmutableListNullableInner(innerImmutableListNullableInner: [String?]?)
  case hasInnerList(innerList: [[KotlinBoolean]])
  case hasInnerNullable(innerList: [[KotlinBoolean?]])
  case hasListInt(hasGeneric: [KotlinInt])
  case hasListIntNullable(hasGeneric: [KotlinInt?])
  case hasListOwn(hasGeneric: [OwnClass])
  case hasListString(hasGeneric: [String])
  case hasListStringNullable(hasGeneric: [String?])
  case hasListStringOuterNullable(hasGeneric: [String]?)
  case hasMap(map: [String : KotlinInt])
  case hasMapNullableOuter(map: [String : KotlinInt]?)
  case hasMapNullableParams(map: [String : KotlinInt?])
  case hasMultipleOwnParams(p1: OwnClass, p2: OwnClass?)
  case hasNestedGeneric(nested: [KotlinPair<NSString, KotlinInt>])
  case hasNullableInnerList(innerList: [[KotlinBoolean]?])
  case hasNullableOuterList(innerList: [[KotlinBoolean]]?)
  case hasOtherNullables(mystring: String, optstring: String?, myfloat: Float32, optfloat: Float32?, mydouble: Float64, optdouble: Float64?)
  case hasOwnClass(ownClass: OwnClass)
  case hasOwnClassWithGeneric(ownClassWithGeneric: OwnHasGeneric<NSString>)
  case hasOwnClassWithGenericAny(ownClassWithGeneric: OwnHasGeneric<AnyObject>)
  case hasOwnClassWithGenericEnum(ownClassWithGeneric: OwnHasGeneric<AnyObject>)
  case hasOwnClassWithGenericInnerMap(ownClassWithGeneric: OwnHasGeneric<NSDictionary>)
  case hasOwnClassWithGenericInnerPair(ownClassWithGeneric: OwnHasGeneric<KotlinPair<NSString, KotlinInt>>)
  case hasOwnClassWithGenericInnerSet(ownClassWithGeneric: OwnHasGeneric<NSSet>)
  case hasOwnClassWithGenericNested(ownClassWithGeneric: OwnHasGeneric<OwnHasGeneric<KotlinInt>>)
  case hasOwnClassWithGenericNullable(ownClassWithGeneric: OwnHasGeneric<KotlinUInt>)
  case hasOwnClassWithGenericThrowable(ownClassWithGeneric: OwnHasGeneric<KotlinThrowable>)
  case hasOwnClassWithGenericWildcard(ownClassWithGeneric: OwnHasGeneric<AnyObject>)
  case hasPairBool(pair: (Bool, Bool?))
  case hasPairFloat(pair: (Float32, Float32?))
  case hasPairGeneric(pair: (UInt8, OwnClass?))
  case hasPairString(pair: (String, String?))
  case hasSet(myset: Set<OwnClass>)
  case hasSetNullableInt(myset: Set<KotlinInt?>)
  case hasSetNullableOuter(myset: Set<KotlinInt?>?)
  case hasSetString(myset: Set<String>)
  case hasSetStringNullable(myset: Set<String?>)
  case hasSomeNullables(myint: Int32, myintopt: Int32?, uintnotoptional: UInt32, uintoptional: UInt32?, mybool: Bool, optbool: Bool?)
  case hasThrowable(throwable: KotlinThrowable)
  case hasTriple(triple: (Float32, Int32?, OwnClass))
  case justAnObj

  public var sealed: TestingSealed {
    switch self {
    case .hasChar(let mychar):
      return shared.HasChar(mychar: mychar.utf16.first!)
    case .hasEnum(let myenum):
      return shared.HasEnum(myenum: myenum)
    case .hasFunction(let myfunc):
      return shared.HasFunction(myfunc: myfunc)
    case .hasImmutableList(let innerImmutableList):
      return shared.HasImmutableList(innerImmutableList: innerImmutableList)
    case .hasImmutableListNullable(let innerImmutableListNullable):
      return shared.HasImmutableListNullable(innerImmutableListNullable: innerImmutableListNullable != nil ? innerImmutableListNullable : nil)
    case .hasImmutableListNullableInner(let innerImmutableListNullableInner):
      return shared.HasImmutableListNullableInner(innerImmutableListNullableInner: innerImmutableListNullableInner != nil ? innerImmutableListNullableInner : nil)
    case .hasInnerList(let innerList):
      return shared.HasInnerList(innerList: innerList)
    case .hasInnerNullable(let innerList):
      return shared.HasInnerNullable(innerList: innerList)
    case .hasListInt(let hasGeneric):
      return shared.HasListInt(hasGeneric: hasGeneric)
    case .hasListIntNullable(let hasGeneric):
      return shared.HasListIntNullable(hasGeneric: hasGeneric)
    case .hasListOwn(let hasGeneric):
      return shared.HasListOwn(hasGeneric: hasGeneric)
    case .hasListString(let hasGeneric):
      return shared.HasListString(hasGeneric: hasGeneric)
    case .hasListStringNullable(let hasGeneric):
      return shared.HasListStringNullable(hasGeneric: hasGeneric)
    case .hasListStringOuterNullable(let hasGeneric):
      return shared.HasListStringOuterNullable(hasGeneric: hasGeneric != nil ? hasGeneric : nil)
    case .hasMap(let map):
      return shared.HasMap(map: map)
    case .hasMapNullableOuter(let map):
      return shared.HasMapNullableOuter(map: map != nil ? map : nil)
    case .hasMapNullableParams(let map):
      return shared.HasMapNullableParams(map: map)
    case .hasMultipleOwnParams(let p1, let p2):
      return shared.HasMultipleOwnParams(p1: p1, p2: p2 != nil ? p2 : nil)
    case .hasNestedGeneric(let nested):
      return shared.HasNestedGeneric(nested: nested)
    case .hasNullableInnerList(let innerList):
      return shared.HasNullableInnerList(innerList: innerList)
    case .hasNullableOuterList(let innerList):
      return shared.HasNullableOuterList(innerList: innerList != nil ? innerList : nil)
    case .hasOtherNullables(let mystring, let optstring, let myfloat, let optfloat, let mydouble, let optdouble):
      return shared.HasOtherNullables(mystring: mystring, optstring: optstring != nil ? optstring : nil, myfloat: myfloat, optfloat: optfloat != nil ? KotlinFloat(value: optfloat!) : nil, mydouble: mydouble, optdouble: optdouble != nil ? KotlinDouble(value: optdouble!) : nil)
    case .hasOwnClass(let ownClass):
      return shared.HasOwnClass(ownClass: ownClass)
    case .hasOwnClassWithGeneric(let ownClassWithGeneric):
      return shared.HasOwnClassWithGeneric(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericAny(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericAny(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericEnum(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericEnum(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericInnerMap(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericInnerMap(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericInnerPair(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericInnerPair(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericInnerSet(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericInnerSet(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericNested(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericNested(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericNullable(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericNullable(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericThrowable(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericThrowable(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericWildcard(let ownClassWithGeneric):
      return shared.HasOwnClassWithGenericWildcard(ownClassWithGeneric: ownClassWithGeneric)
    case .hasPairBool(let pair):
      return shared.HasPairBool(pair: KotlinPair<KotlinBoolean, KotlinBoolean>(first: KotlinBoolean(value: pair.0), second: pair.1 != nil ? KotlinBoolean(value: pair.1!) : nil))
    case .hasPairFloat(let pair):
      return shared.HasPairFloat(pair: KotlinPair<KotlinFloat, KotlinFloat>(first: KotlinFloat(value: pair.0), second: pair.1 != nil ? KotlinFloat(value: pair.1!) : nil))
    case .hasPairGeneric(let pair):
      return shared.HasPairGeneric(pair: KotlinPair<KotlinUByte, shared.OwnClass>(first: KotlinUByte(value: pair.0), second: pair.1 != nil ? pair.1 : nil))
    case .hasPairString(let pair):
      return shared.HasPairString(pair: KotlinPair<NSString, NSString>(first: pair.0 as NSString, second: pair.1 != nil ? pair.1! as NSString : nil))
    case .hasSet(let myset):
      return shared.HasSet(myset: myset)
    case .hasSetNullableInt(let myset):
      return shared.HasSetNullableInt(myset: myset)
    case .hasSetNullableOuter(let myset):
      return shared.HasSetNullableOuter(myset: myset != nil ? myset : nil)
    case .hasSetString(let myset):
      return shared.HasSetString(myset: myset)
    case .hasSetStringNullable(let myset):
      return shared.HasSetStringNullable(myset: myset)
    case .hasSomeNullables(let myint, let myintopt, let uintnotoptional, let uintoptional, let mybool, let optbool):
      return shared.HasSomeNullables(myint: myint, myintopt: myintopt != nil ? KotlinInt(value: myintopt!) : nil, uintnotoptional: uintnotoptional, uintoptional: uintoptional != nil ? KotlinUInt(value: uintoptional!) : nil, mybool: mybool, optbool: optbool != nil ? KotlinBoolean(value: optbool!) : nil)
    case .hasThrowable(let throwable):
      return shared.HasThrowable(throwable: throwable)
    case .hasTriple(let triple):
      return shared.HasTriple(triple: KotlinTriple<KotlinFloat, KotlinInt, shared.OwnClass>(first: KotlinFloat(value: triple.0), second: triple.1 != nil ? KotlinInt(value: triple.1!) : nil, third: triple.2))
    case .justAnObj:
      return shared.JustAnObj()
    }
  }

  public init(_ obj: TestingSealed) {
    if let obj = obj as? shared.HasChar {
      self = .hasChar(mychar: Character(UnicodeScalar(obj.mychar)!))
    } else if let obj = obj as? shared.HasEnum {
      self = .hasEnum(myenum: obj.myenum)
    } else if let obj = obj as? shared.HasFunction {
      self = .hasFunction(myfunc: obj.myfunc)
    } else if let obj = obj as? shared.HasImmutableList {
      self = .hasImmutableList(innerImmutableList: obj.innerImmutableList as! [Swift.String])
    } else if let obj = obj as? shared.HasImmutableListNullable {
      self = .hasImmutableListNullable(innerImmutableListNullable: obj.innerImmutableListNullable != nil ? obj.innerImmutableListNullable as! [Swift.String] : nil)
    } else if let obj = obj as? shared.HasImmutableListNullableInner {
      self = .hasImmutableListNullableInner(innerImmutableListNullableInner: obj.innerImmutableListNullableInner != nil ? obj.innerImmutableListNullableInner as! [Swift.String?] : nil)
    } else if let obj = obj as? shared.HasInnerList {
      self = .hasInnerList(innerList: obj.innerList as! [[shared.KotlinBoolean]])
    } else if let obj = obj as? shared.HasInnerNullable {
      self = .hasInnerNullable(innerList: obj.innerList as! [[shared.KotlinBoolean?]])
    } else if let obj = obj as? shared.HasListInt {
      self = .hasListInt(hasGeneric: obj.hasGeneric as! [shared.KotlinInt])
    } else if let obj = obj as? shared.HasListIntNullable {
      self = .hasListIntNullable(hasGeneric: obj.hasGeneric as! [shared.KotlinInt?])
    } else if let obj = obj as? shared.HasListOwn {
      self = .hasListOwn(hasGeneric: obj.hasGeneric as! [shared.OwnClass])
    } else if let obj = obj as? shared.HasListString {
      self = .hasListString(hasGeneric: obj.hasGeneric as! [Swift.String])
    } else if let obj = obj as? shared.HasListStringNullable {
      self = .hasListStringNullable(hasGeneric: obj.hasGeneric as! [Swift.String?])
    } else if let obj = obj as? shared.HasListStringOuterNullable {
      self = .hasListStringOuterNullable(hasGeneric: obj.hasGeneric != nil ? obj.hasGeneric as! [Swift.String] : nil)
    } else if let obj = obj as? shared.HasMap {
      self = .hasMap(map: obj.map as! [Swift.String : shared.KotlinInt])
    } else if let obj = obj as? shared.HasMapNullableOuter {
      self = .hasMapNullableOuter(map: obj.map != nil ? obj.map as! [Swift.String : shared.KotlinInt] : nil)
    } else if let obj = obj as? shared.HasMapNullableParams {
      self = .hasMapNullableParams(map: obj.map as! [Swift.String : shared.KotlinInt?])
    } else if let obj = obj as? shared.HasMultipleOwnParams {
      self = .hasMultipleOwnParams(p1: obj.p1,
      p2: obj.p2)
    } else if let obj = obj as? shared.HasNestedGeneric {
      self = .hasNestedGeneric(nested: obj.nested as! [shared.KotlinPair<Foundation.NSString, shared.KotlinInt>])
    } else if let obj = obj as? shared.HasNullableInnerList {
      self = .hasNullableInnerList(innerList: obj.innerList as! [[shared.KotlinBoolean]?])
    } else if let obj = obj as? shared.HasNullableOuterList {
      self = .hasNullableOuterList(innerList: obj.innerList != nil ? obj.innerList as! [[shared.KotlinBoolean]] : nil)
    } else if let obj = obj as? shared.HasOtherNullables {
      self = .hasOtherNullables(mystring: obj.mystring as String,
      optstring: obj.optstring != nil ? obj.optstring! as String : nil,
      myfloat: obj.myfloat,
      optfloat: obj.optfloat?.floatValue,
      mydouble: obj.mydouble,
      optdouble: obj.optdouble?.doubleValue)
    } else if let obj = obj as? shared.HasOwnClass {
      self = .hasOwnClass(ownClass: obj.ownClass)
    } else if let obj = obj as? shared.HasOwnClassWithGeneric {
      self = .hasOwnClassWithGeneric(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericAny {
      self = .hasOwnClassWithGenericAny(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericEnum {
      self = .hasOwnClassWithGenericEnum(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericInnerMap {
      self = .hasOwnClassWithGenericInnerMap(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericInnerPair {
      self = .hasOwnClassWithGenericInnerPair(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericInnerSet {
      self = .hasOwnClassWithGenericInnerSet(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericNested {
      self = .hasOwnClassWithGenericNested(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericNullable {
      self = .hasOwnClassWithGenericNullable(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericThrowable {
      self = .hasOwnClassWithGenericThrowable(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasOwnClassWithGenericWildcard {
      self = .hasOwnClassWithGenericWildcard(ownClassWithGeneric: obj.ownClassWithGeneric)
    } else if let obj = obj as? shared.HasPairBool {
      self = .hasPairBool(pair: (obj.pair.first!.boolValue, obj.pair.second?.boolValue))
    } else if let obj = obj as? shared.HasPairFloat {
      self = .hasPairFloat(pair: (obj.pair.first!.floatValue, obj.pair.second?.floatValue))
    } else if let obj = obj as? shared.HasPairGeneric {
      self = .hasPairGeneric(pair: (obj.pair.first!.uint8Value, obj.pair.second))
    } else if let obj = obj as? shared.HasPairString {
      self = .hasPairString(pair: (obj.pair.first! as String, obj.pair.second != nil ? obj.pair.second! as String : nil))
    } else if let obj = obj as? shared.HasSet {
      self = .hasSet(myset: obj.myset as! Set<shared.OwnClass>)
    } else if let obj = obj as? shared.HasSetNullableInt {
      self = .hasSetNullableInt(myset: obj.myset as! Set<shared.KotlinInt?>)
    } else if let obj = obj as? shared.HasSetNullableOuter {
      self = .hasSetNullableOuter(myset: obj.myset != nil ? obj.myset as! Set<shared.KotlinInt?> : nil)
    } else if let obj = obj as? shared.HasSetString {
      self = .hasSetString(myset: obj.myset as! Set<Swift.String>)
    } else if let obj = obj as? shared.HasSetStringNullable {
      self = .hasSetStringNullable(myset: obj.myset as! Set<Swift.String?>)
    } else if let obj = obj as? shared.HasSomeNullables {
      self = .hasSomeNullables(myint: obj.myint,
      myintopt: obj.myintopt?.int32Value,
      uintnotoptional: obj.uintnotoptional,
      uintoptional: obj.uintoptional?.uint32Value,
      mybool: obj.mybool,
      optbool: obj.optbool?.boolValue)
    } else if let obj = obj as? shared.HasThrowable {
      self = .hasThrowable(throwable: obj.throwable)
    } else if let obj = obj as? shared.HasTriple {
      self = .hasTriple(triple: (obj.triple.first!.floatValue, obj.triple.second?.int32Value, obj.triple.third!))
    } else if obj is shared.JustAnObj {
      self = .justAnObj
    } else {
      fatalError("TestingSealedKs not synchronized with TestingSealed class")
    }
  }

}
"""
        println(appendable.toString().split("\n").filter { it.contains("mmutable") }.joinToString(separator = "\n"))
        assertEquals(expected, appendable.toString())
    }
}
