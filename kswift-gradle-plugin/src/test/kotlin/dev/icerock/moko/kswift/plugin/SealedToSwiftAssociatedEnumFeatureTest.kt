package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.LibraryContext
import dev.icerock.moko.kswift.plugin.feature.Filter
import dev.icerock.moko.kswift.plugin.feature.SealedToSwiftAssociatedEnumFeature
import io.outfoxx.swiftpoet.FileSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.resolveSingleFileKlib

@Suppress("LongMethod")
class SealedToSwiftAssociatedEnumFeatureTest {
    @Test
    fun `associated enum feature should produce type mapped output`() {
        // Generated from TestSealed.kt
        val klibPath = this::class.java.classLoader.getResource("associated-enum.klib")
        val konanFile = org.jetbrains.kotlin.konan.file.File(klibPath.toURI().path)
        // Need to use tooling strategy here since the klib was generated with 1.8
        // kotlinc-native TestingSealed.kt -p library -o associated-enum
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
 * selector: ClassContext/associatedenum/com/icerockdev/library/associatedenum/TestingSealed */
public enum TestingSealedKs {

  case hasChar(mychar: Character)
  case hasEnum(myenum: OwnEnum)
  case hasFunction(myfunc: (
    KotlinInt,
    [KotlinBoolean],
    String
  ) -> String)
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
      return HasChar(mychar: mychar.utf16.first!)
    case .hasEnum(let myenum):
      return HasEnum(myenum: myenum)
    case .hasFunction(let myfunc):
      return HasFunction(myfunc: myfunc)
    case .hasInnerList(let innerList):
      return HasInnerList(innerList: innerList)
    case .hasInnerNullable(let innerList):
      return HasInnerNullable(innerList: innerList)
    case .hasListInt(let hasGeneric):
      return HasListInt(hasGeneric: hasGeneric)
    case .hasListIntNullable(let hasGeneric):
      return HasListIntNullable(hasGeneric: hasGeneric)
    case .hasListOwn(let hasGeneric):
      return HasListOwn(hasGeneric: hasGeneric)
    case .hasListString(let hasGeneric):
      return HasListString(hasGeneric: hasGeneric)
    case .hasListStringNullable(let hasGeneric):
      return HasListStringNullable(hasGeneric: hasGeneric)
    case .hasListStringOuterNullable(let hasGeneric):
      return HasListStringOuterNullable(hasGeneric: hasGeneric != nil ? hasGeneric : nil)
    case .hasMap(let map):
      return HasMap(map: map)
    case .hasMapNullableOuter(let map):
      return HasMapNullableOuter(map: map != nil ? map : nil)
    case .hasMapNullableParams(let map):
      return HasMapNullableParams(map: map)
    case .hasMultipleOwnParams(let p1, let p2):
      return HasMultipleOwnParams(p1: p1, p2: p2 != nil ? p2 : nil)
    case .hasNestedGeneric(let nested):
      return HasNestedGeneric(nested: nested)
    case .hasNullableInnerList(let innerList):
      return HasNullableInnerList(innerList: innerList)
    case .hasNullableOuterList(let innerList):
      return HasNullableOuterList(innerList: innerList != nil ? innerList : nil)
    case .hasOtherNullables(let mystring, let optstring, let myfloat, let optfloat, let mydouble, let optdouble):
      return HasOtherNullables(mystring: mystring, optstring: optstring != nil ? optstring : nil, myfloat: myfloat, optfloat: optfloat != nil ? KotlinFloat(value: optfloat!) : nil, mydouble: mydouble, optdouble: optdouble != nil ? KotlinDouble(value: optdouble!) : nil)
    case .hasOwnClass(let ownClass):
      return HasOwnClass(ownClass: ownClass)
    case .hasOwnClassWithGeneric(let ownClassWithGeneric):
      return HasOwnClassWithGeneric(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericAny(let ownClassWithGeneric):
      return HasOwnClassWithGenericAny(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericEnum(let ownClassWithGeneric):
      return HasOwnClassWithGenericEnum(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericInnerMap(let ownClassWithGeneric):
      return HasOwnClassWithGenericInnerMap(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericInnerPair(let ownClassWithGeneric):
      return HasOwnClassWithGenericInnerPair(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericInnerSet(let ownClassWithGeneric):
      return HasOwnClassWithGenericInnerSet(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericNested(let ownClassWithGeneric):
      return HasOwnClassWithGenericNested(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericNullable(let ownClassWithGeneric):
      return HasOwnClassWithGenericNullable(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericThrowable(let ownClassWithGeneric):
      return HasOwnClassWithGenericThrowable(ownClassWithGeneric: ownClassWithGeneric)
    case .hasOwnClassWithGenericWildcard(let ownClassWithGeneric):
      return HasOwnClassWithGenericWildcard(ownClassWithGeneric: ownClassWithGeneric)
    case .hasPairBool(let pair):
      return HasPairBool(pair: KotlinPair<KotlinBoolean, KotlinBoolean>(first: KotlinBoolean(value: pair.0), second: pair.1 != nil ? KotlinBoolean(value: pair.1!) : nil))
    case .hasPairFloat(let pair):
      return HasPairFloat(pair: KotlinPair<KotlinFloat, KotlinFloat>(first: KotlinFloat(value: pair.0), second: pair.1 != nil ? KotlinFloat(value: pair.1!) : nil))
    case .hasPairGeneric(let pair):
      return HasPairGeneric(pair: KotlinPair<KotlinUByte, shared.OwnClass>(first: KotlinUByte(value: pair.0), second: pair.1 != nil ? pair.1 : nil))
    case .hasPairString(let pair):
      return HasPairString(pair: KotlinPair<NSString, NSString>(first: pair.0 as NSString, second: pair.1 != nil ? pair.1! as NSString : nil))
    case .hasSet(let myset):
      return HasSet(myset: myset)
    case .hasSetNullableInt(let myset):
      return HasSetNullableInt(myset: myset)
    case .hasSetNullableOuter(let myset):
      return HasSetNullableOuter(myset: myset != nil ? myset : nil)
    case .hasSetString(let myset):
      return HasSetString(myset: myset)
    case .hasSetStringNullable(let myset):
      return HasSetStringNullable(myset: myset)
    case .hasSomeNullables(let myint, let myintopt, let uintnotoptional, let uintoptional, let mybool, let optbool):
      return HasSomeNullables(myint: myint, myintopt: myintopt != nil ? KotlinInt(value: myintopt!) : nil, uintnotoptional: uintnotoptional, uintoptional: uintoptional != nil ? KotlinUInt(value: uintoptional!) : nil, mybool: mybool, optbool: optbool != nil ? KotlinBoolean(value: optbool!) : nil)
    case .hasThrowable(let throwable):
      return HasThrowable(throwable: throwable)
    case .hasTriple(let triple):
      return HasTriple(triple: KotlinTriple<KotlinFloat, KotlinInt, shared.OwnClass>(first: KotlinFloat(value: triple.0), second: triple.1 != nil ? KotlinInt(value: triple.1!) : nil, third: triple.2))
    case .justAnObj:
      return JustAnObj()
    }
  }

  public init(_ obj: TestingSealed) {
    if let obj = obj as? shared.HasChar {
      self = .hasChar(mychar: Character(UnicodeScalar(obj.mychar)!))
    } else if let obj = obj as? shared.HasEnum {
      self = .hasEnum(myenum: obj.myenum)
    } else if let obj = obj as? shared.HasFunction {
      self = .hasFunction(myfunc: obj.myfunc)
    } else if let obj = obj as? shared.HasInnerList {
      self = .hasInnerList(innerList: obj.innerList as! [[shared.KotlinBoolean]])
    } else if let obj = obj as? shared.HasInnerNullable {
      self = .hasInnerNullable(innerList: obj.innerList as! [[shared.KotlinBoolean]])
    } else if let obj = obj as? shared.HasListInt {
      self = .hasListInt(hasGeneric: obj.hasGeneric as! [shared.KotlinInt])
    } else if let obj = obj as? shared.HasListIntNullable {
      self = .hasListIntNullable(hasGeneric: obj.hasGeneric as! [shared.KotlinInt])
    } else if let obj = obj as? shared.HasListOwn {
      self = .hasListOwn(hasGeneric: obj.hasGeneric as! [shared.OwnClass])
    } else if let obj = obj as? shared.HasListString {
      self = .hasListString(hasGeneric: obj.hasGeneric as! [Swift.String])
    } else if let obj = obj as? shared.HasListStringNullable {
      self = .hasListStringNullable(hasGeneric: obj.hasGeneric as! [Swift.String])
    } else if let obj = obj as? shared.HasListStringOuterNullable {
      self = .hasListStringOuterNullable(hasGeneric: obj.hasGeneric != nil ? obj.hasGeneric as! [[Swift.String]] : nil)
    } else if let obj = obj as? shared.HasMap {
      self = .hasMap(map: obj.map as! [Swift.String : shared.KotlinInt])
    } else if let obj = obj as? shared.HasMapNullableOuter {
      self = .hasMapNullableOuter(map: obj.map != nil ? obj.map as! [Swift.String : shared.KotlinInt] : nil)
    } else if let obj = obj as? shared.HasMapNullableParams {
      self = .hasMapNullableParams(map: obj.map as! [Swift.String : shared.KotlinInt])
    } else if let obj = obj as? shared.HasMultipleOwnParams {
      self = .hasMultipleOwnParams(p1: obj.p1,
      p2: obj.p2)
    } else if let obj = obj as? shared.HasNestedGeneric {
      self = .hasNestedGeneric(nested: obj.nested as! [shared.KotlinPair<Foundation.NSString, shared.KotlinInt>])
    } else if let obj = obj as? shared.HasNullableInnerList {
      self = .hasNullableInnerList(innerList: obj.innerList as! [[shared.KotlinBoolean]])
    } else if let obj = obj as? shared.HasNullableOuterList {
      self = .hasNullableOuterList(innerList: obj.innerList != nil ? obj.innerList as! [[[shared.KotlinBoolean]]] : nil)
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
      self = .hasSetNullableInt(myset: obj.myset as! Set<shared.KotlinInt>)
    } else if let obj = obj as? shared.HasSetNullableOuter {
      self = .hasSetNullableOuter(myset: obj.myset != nil ? obj.myset as! Set<Swift.Set<shared.KotlinInt>> : nil)
    } else if let obj = obj as? shared.HasSetString {
      self = .hasSetString(myset: obj.myset as! Set<Swift.String>)
    } else if let obj = obj as? shared.HasSetStringNullable {
      self = .hasSetStringNullable(myset: obj.myset as! Set<Swift.String>)
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
        assertEquals(expected, appendable.toString())
    }
}
